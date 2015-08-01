package jhangmanclient.gui;

import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javafx.collections.SetChangeListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;

import jhangmanclient.controller.GameController;
import rmi_interface.RMIServer;

public class GamePanel extends HangmanPanel {
    
    Changer changer = null;
    private GameController gameController;

    public GamePanel() {
        super();
        JButton button = new JButton("Log out");
        this.add(button);
        button.addActionListener(e -> this.changer.changePanel("auth"));
        button.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    gameController.handleLogout();
                } catch (RemoteException e1) {
                    showErrorDialog("Couldn't reach the server");
                }
                
            }
        });
    }
    
    public void setChanger(Changer changer) {
        this.changer = changer;
    }

    public void setGameController(GameController controller) {
        this.gameController = controller;
    } 
}
