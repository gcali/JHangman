package jhangmanclient.gui; 

import java.awt.CardLayout;
import java.awt.Container;
import java.awt.HeadlessException;
import java.awt.LayoutManager;

import javax.swing.JFrame;
import javax.swing.JPanel;

import rmi_interface.RMIServer;

public class MainFrame extends JFrame {
    
    class ChangeMainFrame implements Changer {
        private Container[] containers;
        private JFrame frame;
        int currentIndex;

        public ChangeMainFrame(JFrame frame, Container ... containers) {
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
    
    final private static String defaultTitle = "JHangman";
    private JPanel registrationPanel;
    private JPanel gamePanel;
    private JPanel current;
    private RMIServer server;

    public MainFrame(RMIServer server) throws HeadlessException {
        this(server, defaultTitle);
    }

    public MainFrame(RMIServer server, String title) throws HeadlessException {
        super(title); 
        this.server = server;
        init();
    }

    private void init() { 
        createPanels(); 
        this.pack();
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void createPanels() {
        GamePanel gamePanel = new GamePanel(server);
        RegistrationPanel registrationPanel = new RegistrationPanel();
        Changer changer = new ChangeMainFrame(this, gamePanel, registrationPanel);
        registrationPanel.setChanger(changer);
        gamePanel.setChanger(changer); 
        this.registrationPanel = registrationPanel;
        this.gamePanel = gamePanel;
        this.setContentPane(registrationPanel);
    } 
    
    public static void main(String[] args) {
        JFrame frame = new MainFrame(null);
        frame.pack();
        frame.setVisible(true);
    }
}