package jhangmanclient.gui; 

import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;

import jhangmanclient.controller.AuthController;
import jhangmanclient.gui.panels.AuthFrame;
import jhangmanclient.gui.panels.GameChooserFrame;
import jhangmanclient.gui.utility.ChangeMainFrame;
import rmi_interface.RMIServer;

public class GUIMain {
    
    private AuthController authController;
    private Runnable starter;

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
        AuthFrame authFrame = new AuthFrame(
                authController, 
                controller -> gameChooserFrame.setGameController(controller),
                changer);
        changer.addPanel(gameChooserFrame, "gameChooser");
        changer.addPanel(authFrame, "auth");
        this.starter = () -> changer.changeFrame("auth");
    } 
    
    public void start() {
        this.starter.run();
    }
    
    private static void showTopFatalError(String message) {
        JOptionPane.showMessageDialog(new JFrame(), message); 
        System.exit(-1);
    }
    
    public static void main(String[] args) throws RemoteException {
        
        String hostName = "localhost";
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
        GUIMain frame = new GUIMain(authController);
        frame.start();
    } 
}