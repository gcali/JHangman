package jhangmanclient.gui.utility;

import java.awt.Point;

import javax.swing.JFrame;

import jhangmanclient.controller.common.AuthController;
import jhangmanclient.controller.common.GameChooserController;
import jhangmanclient.controller.master.GameMasterController;
import jhangmanclient.controller.player.PlayerController;
import jhangmanclient.gui.frames.AuthFrame;
import jhangmanclient.gui.frames.GameChooserFrame;
import jhangmanclient.gui.frames.GameMasterFrame;
import jhangmanclient.gui.frames.GamePlayerFrame;
import utility.GUIUtils;

public class Switcher {
    
    private AuthFrame authFrame = null;
    private GameChooserFrame gameChooser;
    
    public void showAuth(
        JFrame oldFrame,
        AuthController controller
    ) {
        GUIUtils.invokeAndWait(() -> {
            authFrame = new AuthFrame(controller, Switcher.this);
            authFrame.setLocationRelativeTo(oldFrame);
            disposeOldFrame(oldFrame);
            authFrame.setVisible(true);
        });
    }

    public void showChooser(
        JFrame oldFrame,
        GameChooserController gameChooserController, 
        String nick
    ) {
        GUIUtils.invokeAndWait(() -> {
            if (gameChooser == null) {
                gameChooser = 
                    new GameChooserFrame(gameChooserController, nick, this); 
            } else {
                gameChooser.setGameController(gameChooserController);
            }
            gameChooser.setLocationRelativeTo(oldFrame);
            disposeOldFrame(oldFrame);
            gameChooser.setVisible(true);
        });
    }
    
    public void showPlayer(
        JFrame oldFrame,
        PlayerController controller
    ) {
        GUIUtils.invokeAndWait(() -> {
            GamePlayerFrame playerFrame = new GamePlayerFrame(controller);
            playerFrame.setLocationRelativeTo(oldFrame);
            playerFrame.setVisible(true);
        });
    }
    
    public void showMaster(
        JFrame oldFrame,
        GameMasterController controller
    ) {
        GUIUtils.invokeAndWait(() -> {
            GameMasterFrame masterFrame = new GameMasterFrame(controller);
            masterFrame.setLocationRelativeTo(oldFrame);
            masterFrame.setVisible(true);
        });
    }
    
    private static void disposeOldFrame(JFrame oldFrame) {
        if (oldFrame != null) {
            oldFrame.setVisible(false);
        }
    }
    
    public void showAuth(JFrame oldFrame) {
        if (authFrame != null) {
            GUIUtils.invokeAndWait(() -> {
                Point location = oldFrame.getLocation();
                disposeOldFrame(oldFrame);
                authFrame.setVisible(true);
                authFrame.setLocation(location);
            });
        }
    } 
}