package jhangmanclient.gui; 

import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;

import jhangmanclient.controller.AuthController;
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
        int borderSize = 10;
        Border emptyBorder = BorderFactory.createEmptyBorder(borderSize, 
                                                             borderSize, 
                                                             borderSize, 
                                                             borderSize);
        GameChoosePanel gameChoosePanel = new GameChoosePanel();
        gameChoosePanel.setBorder(emptyBorder);
        AuthPanel authPanel = AuthPanel.create(
                authController, 
                controller -> gameChoosePanel.setGameController(controller));
        authPanel.setBorder(emptyBorder);
        JFrame gameFrame = createFrameFromPanel(gameChoosePanel);
        JFrame authFrame = createFrameFromPanel(authPanel);
        ChangeMainFrame changer = 
                new ChangeMainFrame();
        changer.addPanel(gameFrame, "gameChooser");
        changer.addPanel(authFrame, "auth");
        this.starter = () -> changer.changePanel("auth");
        authPanel.setChanger(changer);
        gameChoosePanel.setChanger(changer); 
    } 
    
    private static JFrame createFrameFromPanel(Container content) {
        JFrame frame = new JFrame("JHangman");
        frame.setContentPane(content);
        frame.pack();
        int minimumWidth, minimumHeight;
        minimumWidth = frame.getWidth();
        minimumHeight = frame.getHeight();
        frame.setMinimumSize(new Dimension(minimumWidth, minimumHeight));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        return frame;
    }
    
    public void start() {
        this.starter.run();
    }
    
    private static void showTopFatalError(String message) {
        JOptionPane.showMessageDialog(new JFrame(), message); 
        System.exit(-1);
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e1) {
            System.err.println("Couldn't set look and feel");
        }
        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry(RMIServer.defaultPort);
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