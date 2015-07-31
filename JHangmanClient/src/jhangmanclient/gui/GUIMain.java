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
    
    class ChangeMainFrameContent implements Changer {
        private Container[] containers;
        private JFrame frame;
        int currentIndex;

        public ChangeMainFrameContent(JFrame frame, Container ... containers) {
            this.frame = frame;
            this.containers = containers;
            this.currentIndex = 0;
        }

        public void changePanel() {
            this.frame.setContentPane(containers[this.currentIndex]); 
            this.frame.pack();
            int minimumWidth, minimumHeight;
            minimumWidth = this.frame.getWidth();
            minimumHeight = this.frame.getHeight();
            this.frame.setMinimumSize(new Dimension(minimumWidth, 
                                                    minimumHeight));
            System.out.println("New panel: " + this.currentIndex);
            this.currentIndex = (this.currentIndex + 1) % this.containers.length;
            this.frame.revalidate();
            this.frame.repaint();
        }
    }
    
    class ChangeMainFrame implements Changer {
        private JFrame[] frames;
        private int currentIndex;
        
        public ChangeMainFrame(JFrame ... frames) {
            this.frames = frames;
            this.currentIndex = 0;
        }
        
        public void changePanel() {
            this.frames[currentIndex++].setVisible(false);
            this.currentIndex = this.currentIndex % this.frames.length;
            this.frames[currentIndex].setVisible(true);
        }
    }
    
    
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
        GamePanel gamePanel = new GamePanel();
        gamePanel.setBorder(emptyBorder);
        AuthPanel authPanel = AuthPanel.create(
                authController, 
                controller -> gamePanel.setGameController(controller));
        authPanel.setBorder(emptyBorder);
        JFrame gameFrame = createFrameFromPanel(gamePanel);
        JFrame registrationFrame = createFrameFromPanel(authPanel);
        Changer changer = new ChangeMainFrame(gameFrame, registrationFrame);
        this.starter = () -> changer.changePanel();
        authPanel.setChanger(changer);
        gamePanel.setChanger(changer); 
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
    
    @Deprecated
    private static void showTopFatalError(String message, Exception e) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel("<html><p>" + message + "</p></html>");
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        JTextArea exceptionArea = new JTextArea(writer.toString());
        JScrollPane pane = new JScrollPane(exceptionArea);
        pane.setMaximumSize(new Dimension(1000,400));
        pane.setHorizontalScrollBarPolicy(pane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane.setVerticalScrollBarPolicy(pane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(label);
        panel.add(pane);
        JOptionPane.showMessageDialog(new JFrame(), panel);
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