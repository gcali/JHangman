package jhangmanclient.main; 

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import jhangmanclient.controller.common.AuthController;
import jhangmanclient.gui.frames.AuthFrame;
import jhangmanclient.gui.frames.GameChooserFrame;
import jhangmanclient.gui.utility.ChangeMainFrame;
import rmi_interface.RMIServer;

public class GUIMain {
    
    private AuthController authController;
    private Runnable starter;
    private Consumer<User> forceLogin;

    public GUIMain(AuthController authController) throws HeadlessException {
        this.authController = authController;
        init();
    }

    private void init() { 
        createPanels(); 
    }

    private void createPanels() {
        ChangeMainFrame changer = new ChangeMainFrame();
        GameChooserFrame gameChooserFrame = new GameChooserFrame(changer);
        changer.addObserver(gameChooserFrame);
        AuthFrame authFrame = new AuthFrame(
                authController, 
                controller -> gameChooserFrame.setGameController(controller),
                changer);
        changer.addPanel(gameChooserFrame, "gameChooser");
        changer.addPanel(authFrame, "auth");
        this.starter = () -> changer.changeFrame("auth");
        this.forceLogin = new Consumer<User>() {

            @Override
            public void accept(User t) {
                authFrame.setNick(t.getNick());
                authFrame.setPass(t.getPass());
                authFrame.actionPerformed(new ActionEvent(authFrame, 0, "login")); 
            }
        };
    } 
    
    public void start() {
        java.awt.EventQueue.invokeLater(new Runnable() { 
            @Override
            public void run() {
                GUIMain.this.starter.run(); 
            }
        });
    }
    
    void startLogged(String nick, String pass) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                GUIMain.this.forceLogin.accept(new User(nick, pass)); 
            }
        });
    }
    
    private static void showTopFatalError(String message) {
        JOptionPane.showMessageDialog(new JFrame(), message); 
        System.exit(-1);
    }
    
    public static void main(String[] args) throws RemoteException {
        
        String hostName = "localhost";
        try {
            hostName = args[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            
        }
        AuthController authController = initConnection(hostName);
        GUIMain frame = new GUIMain(authController);
        frame.start();
    }

    static AuthController initConnection(String hostName) throws RemoteException {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e1) {
            System.err.println("Couldn't set look and feel");
        }
        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry(hostName, 
                                                  RMIServer.defaultPort);
        } catch (RemoteException e) {
            System.err.println("Connection error");
            showTopFatalError("Couldn't reach the server");
        }
        
        RMIServer server = null;
        try {
            server = (RMIServer) registry.lookup(RMIServer.name);
        } catch (RemoteException e) {
            e.printStackTrace();
            showTopFatalError("Couldn't reach the server");
        } catch (NotBoundException e) {
            System.err.println("No " + RMIServer.name + " found");
            e.printStackTrace();
            showTopFatalError("Couldn't reach the server");
        }
        
        assert server != null;
        
        AuthController authController = new AuthController(server);
        return authController;
    } 
    
    public static class User {
        private String nick;
        private String pass;

        public User(String nick, String pass) {
            this.nick = nick;
            this.pass = pass;
        }

        public String getNick() {
            return nick;
        }

        public String getPass() {
            return pass;
        }
    }
}