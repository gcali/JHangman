package jhangmanclient.gui.frames;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;

import javax.swing.Box;
import javax.swing.JFrame;

import jhangmanclient.controller.master.GameMasterController;
import jhangmanclient.gui.components.WordChooser;
import jhangmanclient.gui.components.WordSubmittedEvent;
import utility.Loggable;
import utility.observer.JHObserver;
import utility.observer.ObservationHandler;

public class GameMasterFrame extends HangmanFrame 
                             implements JHObserver,
                                        Loggable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private GameMasterController controller;
    private WordChooser wordChooser;
    private GameUpdates gameUpdates;

    private final Runnable activateFrameAfterAbort;

    public GameMasterFrame(
        GameMasterController controller, 
        Runnable activateFrameAfterAbort
    ) {
        super();
        this.controller = controller;
        this.activateFrameAfterAbort = activateFrameAfterAbort;
        this.placeComponents();
        this.terminateInitialization();
    }
    
    @Override
    protected void initComponents() {
        this.wordChooser = new WordChooser();
        this.gameUpdates = new GameUpdates(controller);
        this.wordChooser.addObserver(this);
    } 
    
    @ObservationHandler
    public void onWordSubmittedEvent(WordSubmittedEvent e) {
        this.controller.setWord(e.getWord());
        try {
            this.controller.initConnection();
            this.showGame();
        } catch (IOException e1) {
            this.printError("Couldn't init controller, should retry");
            this.wordChooser.reset();
        }
    }
    
    private void showGame() {
        // TODO Auto-generated method stub
        
    }

    private void placeComponents() {
        this.add(this.wordChooser);
        this.add(Box.createVerticalStrut(10));
        this.add(this.gameUpdates);
    }
    
    private void terminateInitialization() {
        this.pack();
        this.addWindowListener(new WindowAdapter() { 
            @Override
            public void windowClosing(WindowEvent e) {
                GameMasterFrame.this.abort();
            }
            
        });
    }
    
    public void abort() {
        if (this.controller != null) {
            this.controller.close();
        }
        this.activateFrameAfterAbort.run();
    }
    
    @Override
    public String getLoggableId() {
        return "GameMasterFrame";
    }
    
    public static void main(String[] args) throws IOException {
        String addressName = "239.255.54.67";
        int port = 49312;
        InetAddress address = InetAddress.getByName(addressName);
        GameMasterController controller = new GameMasterController("gio", address, port, "ciao", 5);
        //TODO stub
        JFrame frame = new GameMasterFrame(controller, () -> {});
        frame.setVisible(true);
    }
}