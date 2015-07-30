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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;

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
    
    
    private RMIServer remoteServer;
    private Runnable starter;

    public GUIMain(RMIServer remoteServer) throws HeadlessException {
        this.remoteServer = remoteServer;
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
        GamePanel gamePanel = new GamePanel(remoteServer);
        gamePanel.setBorder(emptyBorder);
        RegistrationPanel registrationPanel = RegistrationPanel.create();
        registrationPanel.setBorder(emptyBorder);
        JFrame gameFrame = createFrameFromPanel(gamePanel);
        JFrame registrationFrame = createFrameFromPanel(registrationPanel);
        Changer changer = new ChangeMainFrame(gameFrame, registrationFrame);
        this.starter = () -> changer.changePanel();
        registrationPanel.setChanger(changer);
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
    
    public static void main(String[] args) throws ClassNotFoundException, 
                                                  InstantiationException, 
                                                  IllegalAccessException, 
                                                  UnsupportedLookAndFeelException {
        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry(RMIServer.defaultPort);
        } catch (RemoteException e) {
            System.out.println("Connection error");
            throw new RuntimeException(e);
        }
        
        RMIServer server = null;
        try {
            server = (RMIServer) registry.lookup(RMIServer.name);
        } catch (RemoteException e) {
            System.err.println("Connection error");
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            System.err.println("No " + RMIServer.name + " found");
            throw new RuntimeException(e);
        }
        
        assert server != null;
        
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        GUIMain frame = new GUIMain(server);
        frame.start();
    }

}