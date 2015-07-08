package jhangmanclient.gui;

import java.awt.LayoutManager;

import javafx.collections.SetChangeListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;

import rmi_interface.RMIServer;

public class GamePanel extends JPanel {
    
    Changer changer = null;
    RMIServer server;

    public GamePanel(RMIServer server) {
        super();
        this.server = server;
        JButton button = new JButton("Cliccami");
        this.add(button);
        button.addActionListener(e -> this.changer.changePanel());
    }
    
    public void setChanger(Changer changer) {
        this.changer = changer;
    } 
}
