package jhangmanclient.gui.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.swing.JButton;

import jhangmanclient.controller.GameChooserController;
import jhangmanclient.gui.utility.Changer;

public class GameChooserFrame extends HangmanFrame {
    
    public final static String idString = "gameChooser";
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    Changer changer = null;
    private GameChooserController gameChooserController;

    public GameChooserFrame(Changer changer) {
        super(10);
        this.changer = changer;
        this.setVisible(false);
    }
    
    @Override
    protected void initComponents() {
        JButton button = new JButton("Log out");
        this.add(button);
        button.addActionListener(e -> this.changer.changeFrame("auth"));
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
    
    public void setGameController(GameChooserController controller) {
        this.gameChooserController = controller;
    } 
}
