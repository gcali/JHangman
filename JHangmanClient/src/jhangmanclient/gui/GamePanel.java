package jhangmanclient.gui;

import java.awt.LayoutManager;

import javafx.collections.SetChangeListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;

import jhangmanclient.controller.GameController;
import rmi_interface.RMIServer;

public class GamePanel extends JPanel {
    
    Changer changer = null;
    private GameController gameController;

    public GamePanel() {
        super();
        JButton button = new JButton("Log out");
        this.add(button);
        button.addActionListener(e -> this.changer.changePanel());
    }
    
    public void setChanger(Changer changer) {
        this.changer = changer;
    }

    public void setGameController(GameController controller) {
        this.gameController = controller;
    } 
}
