package jhangmanclient.gui.utility;

import jhangmanclient.controller.common.AuthController;
import jhangmanclient.controller.common.GameChooserController;
import jhangmanclient.controller.master.GameMasterController;
import jhangmanclient.controller.player.PlayerController;
import jhangmanclient.gui.frames.AuthFrame;
import jhangmanclient.gui.frames.GameChooserFrame;
import jhangmanclient.gui.frames.GameMasterFrame;
import jhangmanclient.gui.frames.GamePlayerFrame;
import jhangmanclient.gui.frames.HangmanFrame;
import utility.GUIUtils;

public class Switcher {
    
    private AuthFrame authFrame = null;
    private GameChooserFrame gameChooser;
    
    public void showAuth(
        HangmanFrame oldFrame,
        AuthController controller
    ) {
        disposeOldFrame(oldFrame);
        authFrame = new AuthFrame(controller, this);
        authFrame.setVisible(true);
    }

    public void showChooser(
        HangmanFrame oldFrame,
        GameChooserController gameChooserController, 
        String nick
    ) {
        disposeOldFrame(oldFrame);
        if (gameChooser == null) {
            gameChooser = new GameChooserFrame(gameChooserController, nick, this); 
        } else {
            gameChooser.setGameController(gameChooserController);
        }
        GUIUtils.invokeAndWait(() -> gameChooser.setVisible(true));
    }
    
    public void showPlayer(
        PlayerController controller
    ) {
        GamePlayerFrame playerFrame = new GamePlayerFrame(controller);
        playerFrame.setVisible(true);
    }
    
    public void showMaster(
        GameMasterController controller
    ) {
        GameMasterFrame masterFrame = new GameMasterFrame(controller);
        masterFrame.setVisible(false);
    }
    
    private static void disposeOldFrame(HangmanFrame oldFrame) {
        if (oldFrame != null) {
            oldFrame.setVisible(false);
        }
    }

    public void showAuth(HangmanFrame oldFrame) {
        if (authFrame != null) {
            disposeOldFrame(oldFrame);
            authFrame.setVisible(true);
        }
    } 
}