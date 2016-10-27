package jhangmanclient.gui.frames;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import jhangmanclient.controller.master.GameMasterController;
import jhangmanclient.controller.master.LetterGuessedEvent;
import jhangmanclient.controller.master.LostGameEvent;
import jhangmanclient.controller.master.WordGuessedEvent;
import jhangmanclient.controller.master.WrongGuessEvent;
import jhangmanclient.gui.components.ActionsPanel;
import jhangmanclient.gui.components.LivesIndicatorPanel;
import jhangmanclient.gui.components.NotificationPanel;
import jhangmanclient.gui.components.WordInputPanel;
import jhangmanclient.gui.components.WordPlayerDisplay;
import utility.GUIUtils;
import utility.observer.JHObserver;
import utility.observer.ObservationHandler;

public class GameMasterFrame extends HangmanFrame 
                             implements JHObserver {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private GameMasterController controller;

    private NotificationPanel notificationPanel;

    private LivesIndicatorPanel livesPanel;

    private JButton submitButton;

    private JButton closeButton;

    private WordInputPanel inputPanel;

    private WordPlayerDisplay wordPanel;
    
    private Thread timeoutThread;
    
    public GameMasterFrame(GameMasterController controller) {
        super(10);
        timeoutThread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(controller.getGameTimeout());
                    if (controller != null) {
                        controller.close();
                        submitButton.setEnabled(false);
                        inputPanel.setEditable(false);
                        GUIUtils.invokeLater(() -> notificationPanel.addLine(
                            "Game closed after long timeout"
                        ));
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        this.controller = controller;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setUpActions();
        terminateInitialization();
        controller.addObserver(this);
        timeoutThread.start();
    }
    
    private void setUpActions() {
        submitButton.addActionListener(e -> {
            String text = inputPanel.getText();
            controller.setWord(text);
            try {
                controller.start();
                if (text != null && !text.equals("")) {
                    submitButton.setEnabled(false);
                    inputPanel.setEditable(false);
                }
                wordPanel.setWordLength(text.length());
            } catch (IOException e1) {
                notificationPanel.addLine("Connection error, please retry");
            }
        });
        closeButton.addActionListener(e -> close());
    }

    private void close() {
        if (timeoutThread != null && timeoutThread.isAlive()) {
            timeoutThread.interrupt();
        }
        controller.close();
        GUIUtils.invokeLater(() -> GameMasterFrame.this.dispose());
    }

    @Override
    protected void initComponents() {
        JPanel leftPanel = new JPanel() {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(
                    getPreferredSize().width,
                    super.getMaximumSize().height
                );
            }
        };
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
        
        livesPanel = new LivesIndicatorPanel(10);
        
        wordPanel = new WordPlayerDisplay(8);
        wordPanel.setFontSize(40);
        inputPanel = new WordInputPanel();
        inputPanel.setFontSize(40);
        
        submitButton = new JButton("Submit");
        closeButton = new JButton("Close");
        
        JButton [] leftButtons = {closeButton};
        JButton [] rightButtons = {submitButton};
        
        JPanel buttonPanel = new ActionsPanel(leftButtons, rightButtons);
        
        leftPanel.add(new JPanel());
        leftPanel.add(livesPanel);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(wordPanel);
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(inputPanel);
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(buttonPanel);
        
        notificationPanel = new NotificationPanel(20); 

        this.add(leftPanel);
        this.add(Box.createHorizontalStrut(5));
        this.add(notificationPanel);
    } 
    
    @Override
    protected void initLayout() {
        getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.LINE_AXIS));
    }
    
    public void setWord(String word) {
        this.controller.setWord(word);
        try {
            this.controller.start();
        } catch (IOException e1) {
            this.printError("Couldn't init controller, should retry");
        }
    }
    
    private void terminateInitialization() {
        livesPanel.setLives(controller.getMaxLives());
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
        this.dispose();
    }
    
    @Override
    public String getLoggableId() {
        return "GameMasterFrame";
    }
    
    public static void main(String[] args) throws IOException {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e1) {
            System.err.println("Couldn't set look and feel");
        }
        String addressName = "239.255.54.67";
        int port = 49312;
        InetAddress address = InetAddress.getByName(addressName);
        GameMasterController controller = new GameMasterController("master", address, port, "ciao", 2, 5000000);
        System.out.println(controller);
        //TODO stub
        JFrame frame = new GameMasterFrame(controller);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GUIUtils.invokeLater(() -> frame.setVisible(true));
    }
    
    @ObservationHandler
    public void onLetterGuessedEvent(LetterGuessedEvent e) {
        GUIUtils.invokeLater(() -> notificationPanel.addLine("Letter guessed"));
    }
    
    @ObservationHandler
    public void onWordGuessedEvent(WordGuessedEvent e) {
        GUIUtils.invokeLater(() -> {
            notificationPanel.addLine("Word guessed by " + e.getWinnerNick() +
                "!");
            submitButton.setEnabled(false);

        });
    }
    
    public void onWrongGuessEvent(WrongGuessEvent e) {
        GUIUtils.invokeLater(() -> {
            notificationPanel.addLine("Wrong guess");
        });

    }
    
    public void onLostGameEvent(LostGameEvent e) {
        GUIUtils.invokeLater(() -> {
            notificationPanel.addLine("Game over!");
            submitButton.setEnabled(false);
        });
    }
}