package jhangmanclient.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;


import javax.swing.JButton;

import jhangmanclient.controller.GameController;

public class GameChoosePanel extends HangmanPanel {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    Changer changer = null;
    private GameController gameController;

    public GameChoosePanel() {
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
