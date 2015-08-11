package jhangmanclient.gui.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.swing.JButton;

import jhangmanclient.controller.GameChooserController;
import jhangmanclient.gui.utility.Changer;

public class GameChooserPanel extends HangmanPanel {
    
    public final static String idString = "gameChooser";
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    Changer changer = null;
    private GameChooserController gameChooserController;

    public GameChooserPanel() {
        super();
        JButton button = new JButton("Log out");
        this.add(button);
        button.addActionListener(e -> this.changer.changePanel("auth"));
        button.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    gameChooserController.handleLogout();
                } catch (RemoteException e1) {
                    showErrorDialog("Couldn't reach the server");
                }
                
            }
        });
    }
    
    public void setChanger(Changer changer) {
        this.changer = changer;
    }

    public void setGameController(GameChooserController controller) {
        this.gameChooserController = controller;
    } 
}
