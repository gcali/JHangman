package jhangmanclient.main; 

import java.awt.event.ActionEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import jhangmanclient.config.ClientConfigData;
import jhangmanclient.controller.common.AuthController;
import jhangmanclient.gui.frames.AuthFrame;
import jhangmanclient.gui.utility.Switcher;
import rmi_interface.RMIServer;
import utility.GUIUtils;

public class GUIMain {
    
    public static void start(AuthController controller) {
        GUIUtils.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                Switcher switcher = new Switcher();
                switcher.showAuth(null, controller);
            }
        });
    }
    
    static AuthFrame startLogged(String nick, String pass, AuthController controller) {
        Switcher switcher = new Switcher();
        AuthFrame frame = new AuthFrame(controller, switcher);
        GUIUtils.invokeAndWait(new Runnable() {
            
            @Override
            public void run() {
                switcher.setAuth(frame);
                frame.setNick(nick);
                frame.setPass(pass);
                frame.actionPerformed(new ActionEvent(frame, 0, "login"));
            }
        });
        return frame;
    }
    
    private static void showTopFatalError(String message) {
        JOptionPane.showMessageDialog(new JFrame(), message); 
        System.exit(-1);
    }
    
    public static void main(String[] args) throws RemoteException {
        
        ClientConfigData configData = new ClientConfigData();
        System.out.println(configData.getRmiAddress());
        try {
            configData.setRmiAddress(args[0]);
            try {
                configData.setRmiPort(Integer.parseInt(args[1])); 
            } catch (NumberFormatException e) {
            }
            configData.setTcpAddress(args[2]);
            try {
                configData.setTcpPort(Integer.parseInt(args[3])); 
            } catch (NumberFormatException e) {
                
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        AuthController authController = 
            initConnection(configData);
        start(authController);
    }

    static AuthController initConnection(ClientConfigData configData) 
        throws RemoteException {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e1) {
            System.err.println("Couldn't set look and feel");
        }
        Registry registry = null;
        System.out.println(configData.getRmiAddress());
        try {
            registry = LocateRegistry.getRegistry(configData.getRmiAddress(),
                                                  configData.getRmiPort());
        } catch (RemoteException e) {
            System.err.println("Connection error");
            showTopFatalError("Couldn't reach the server");
        }
        
        RMIServer server = null;
        try {
            server = (RMIServer) registry.lookup(configData.getRmiName());
        } catch (RemoteException e) {
            e.printStackTrace();
            showTopFatalError("Couldn't reach the server");
        } catch (NotBoundException e) {
            System.err.println("No " + configData.getRmiName() + " found");
            e.printStackTrace();
            showTopFatalError("Couldn't reach the server");
        }
        
        assert server != null;
        
        AuthController authController = new AuthController(server, configData);
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