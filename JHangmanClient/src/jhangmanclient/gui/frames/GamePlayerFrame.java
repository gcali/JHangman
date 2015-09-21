package jhangmanclient.gui.frames;

import jhangmanclient.controller.PlayerController;

public class GamePlayerFrame extends HangmanFrame {
    
    private PlayerController controller;

    public GamePlayerFrame(PlayerController controller) {
        this.controller = controller;
    }

    @Override
    protected void initComponents() {
    }

}