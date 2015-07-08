package jhangmanclient.gui; 

import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;

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
    
    
    private RMIServer server;
    private Runnable starter;

    public GUIMain(RMIServer server) throws HeadlessException {
        this.server = server;
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
        GamePanel gamePanel = new GamePanel(server);
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
    
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        GUIMain frame = new GUIMain(null);
        frame.start();
    }
}