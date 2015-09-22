package jhangmanclient.gui.frames;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import jhangmanclient.controller.master.GameMasterController;

public class GameUpdates extends JScrollPane {
    
    private GameMasterController controller;
    private JTextArea textArea;

    public GameUpdates(GameMasterController controller) {
        super(VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
        this.controller = controller;
        initComponents();
        placeComponents();
    }

    private void placeComponents() {
        this.getViewport().setView(this.textArea);
    }

    private void initComponents() {
        this.textArea = new JTextArea(5, 50);
        this.textArea.setLineWrap(true);
        this.textArea.setWrapStyleWord(true);
        this.textArea.setEditable(false);
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.add(new GameUpdates(null));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
