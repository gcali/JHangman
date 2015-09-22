package jhangmanclient.gui.frames;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import jhangmanclient.controller.master.GameMasterController;
import jhangmanclient.gui.components.WordChooser;
import jhangmanclient.gui.components.WordSubmittedEvent;
import utility.observer.JHObserver;
import utility.observer.ObservationHandler;

public class GameMasterFrame extends HangmanFrame {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private GameMasterController controller;
    private WordChooser wordChooser;
    private GameUpdates gameUpdates;

    public GameMasterFrame(GameMasterController controller) {
        super();
        this.controller = controller;
        this.placeComponents();
        this.terminateInitialization();
    }
    
    @Override
    protected void initComponents() {
        this.wordChooser = new WordChooser();
        this.gameUpdates = new GameUpdates(controller);
        this.wordChooser.addObserver(new JHObserver() { 

            @ObservationHandler
            public void onWordSubmittedEvent(WordSubmittedEvent e) {
                controller.setWord(e.getWord());
            }
        });
    } 
    
    protected void placeComponents() {
        this.add(this.wordChooser);
        this.add(Box.createVerticalStrut(10));
        this.add(this.gameUpdates);
    }
    
    protected void terminateInitialization() {
        this.pack();
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); 
    }
    
    public static void main(String[] args) {
        JFrame frame = new GameMasterFrame(new GameMasterController("ciao", null, "ciao", 10));
        frame.setVisible(true);
    }
}