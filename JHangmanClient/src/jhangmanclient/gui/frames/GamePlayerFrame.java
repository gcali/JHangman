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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import jhangmanclient.controller.player.AckedMessageEvent;
import jhangmanclient.controller.player.GameAbortedEvent;
import jhangmanclient.controller.player.GameLostEvent;
import jhangmanclient.controller.player.GameWonEvent;
import jhangmanclient.controller.player.GuessCollisionException;
import jhangmanclient.controller.player.PlayerController;
import jhangmanclient.controller.player.UpdatedPlayingStatusEvent;
import jhangmanclient.gui.components.ActionsPanel;
import jhangmanclient.gui.components.EmptyFieldEvent;
import jhangmanclient.gui.components.LivesIndicatorPanel;
import jhangmanclient.gui.components.NonEmptyFieldEvent;
import jhangmanclient.gui.components.NotificationPanel;
import jhangmanclient.gui.components.WordInputPanel;
import jhangmanclient.gui.components.WordPlayerDisplay;
import utility.GUIUtils;
import utility.observer.JHObserver;
import utility.observer.ObservationHandler;

public class GamePlayerFrame extends HangmanFrame
                             implements JHObserver {
    
    private PlayerController controller;
    private LivesIndicatorPanel livesPanel;
    private WordPlayerDisplay wordPanel;
    private NotificationPanel notificationPanel;
    private JButton abortButton;
    private JButton guessButton;
    private WordInputPanel inputPanel;

    public GamePlayerFrame(PlayerController controller) {
        super(10);
        this.controller = controller;
        controller.start();
        setUpActions();
        
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }
    
    private void setUpActions() {
        this.abortButton.addActionListener(e -> abort());
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                abort();
            }
        });
        
        this.guessButton.addActionListener(e -> { 
            String text = inputPanel.getText();
            if (text != null && text.length() > 0) { 
                guessButton.setEnabled(false);
                if (text.length() == 1) {
                    guessLetter(text.charAt(0));
                } else {
                    guessWord(text);
                }
            }
        });
        
        controller.addObserver(this);
    }

    protected void abort() {
        controller.close();
        this.dispose();
    }

    @Override
    protected void initLayout() {
        super.initLayout();
        this.setLayout(
            new BoxLayout(this.getContentPane(), BoxLayout.LINE_AXIS)
        );
    }

    @Override
    protected void initComponents() { 

        JPanel leftPanel = createMinimumWidthPanel();

        JPanel buttonPanel = createButtonPanel(); 
        livesPanel = new LivesIndicatorPanel(0); 
        wordPanel = new WordPlayerDisplay(8); 
        wordPanel.setFontSize(40);
        inputPanel = new WordInputPanel();
        inputPanel.setFontSize(40);
        inputPanel.addObserver(new JHObserver() {
            @ObservationHandler
            public void onEmptyFieldEvent(EmptyFieldEvent e) {
                guessButton.setEnabled(false);
            }
            
            @ObservationHandler
            public void onNonEmptyFieldEvent(NonEmptyFieldEvent e) {
                guessButton.setEnabled(true);
            }
        });

        leftPanel.add(new JPanel());
        leftPanel.add(livesPanel);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(wordPanel);
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(inputPanel);
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(buttonPanel);

        notificationPanel = new NotificationPanel();
        
        this.add(leftPanel);
        this.add(Box.createHorizontalStrut(5));
        this.add(notificationPanel);
     
    }
    
    private JPanel createMinimumWidthPanel() {
        JPanel panel = new JPanel() {
            public Dimension getMaximumSize() {
                Dimension preferred = getPreferredSize();
                return new Dimension(
                    preferred.width, 
                    super.getMaximumSize().height
                );
            };
        }; 
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        return panel; 
    }

    private JPanel createButtonPanel() {
        abortButton = new JButton("Close");
        guessButton = new JButton("Guess");
        guessButton.setEnabled(false);
        
        JButton[] leftButtons = {abortButton};
        JButton[] rightButtons = {guessButton};
        
        ActionsPanel actionPanel = new ActionsPanel(leftButtons, rightButtons);
        return actionPanel;
    }
    
    
    public void setWord(String word) {
        wordPanel.setWord(word);
    }
    
    public void setWordLength(int length) {
        wordPanel.setWordLength(length);
    }

    @Override
    public String getLoggableId() {
        return "GamePlayerFrame";
    }

    private void guessWord(String text) {
        try {
            controller.sendGuessToBeAcked(text);
        } catch (IOException e) {
            printError("IOException");
        } catch (GuessCollisionException e) {
            printError("Guess still not acked");
        }
    }

    private void guessLetter(char letter) {
        try {
            controller.sendGuessToBeAcked(letter);
        } catch (IOException e) {
            printError("IOException");
        } catch (GuessCollisionException e) {
            printError("Guess still not acked");
        }
    }
    
    @ObservationHandler
    public void onUpdatedPlayingStatusEvent(UpdatedPlayingStatusEvent event) {
        GUIUtils.invokeAndWait(() -> {
            String eventWord = event.getWord();
            livesPanel.setLives(event.getMaxLives());
            if (wordPanel.isUpdate(eventWord)) {
                wordPanel.setWord(event.getWord());
            }
            notificationPanel.addLine(
                "Update! Lives: " + 
                event.getRemainingLives() + "/" +
                event.getMaxLives()); 
        });
    }
    
    @ObservationHandler
    public void onGameWonEvent(GameWonEvent e) { 
        String nick = e.getWinnerNick();
        GUIUtils.invokeAndWait(() -> {
            guessButton.setEnabled(false);
            inputPanel.setEditable(false);
            wordPanel.setWord(e.getVisibleWord());
            if (nick != null && nick.equals(controller.getNick())) {
                wordPanel.setWinner();
            } else {
                wordPanel.setLoser();
            }
        });
        notificationPanel.addLine("Player " + nick + " has won!");
    }
    
    @ObservationHandler
    public void onGameLostEvent(GameLostEvent e)  {
        GUIUtils.invokeAndWait(() -> {
            guessButton.setEnabled(false);
            inputPanel.setEditable(false);
            wordPanel.setWord(e.getWord());
            wordPanel.setLoser();
        });
        notificationPanel.addLine("Game was lost...");
    }
    
    @ObservationHandler
    public void onGameAbortedEvent(GameAbortedEvent e) {
        GUIUtils.invokeAndWait(() -> {
            guessButton.setEnabled(false);
            inputPanel.setEditable(false);
        });
        notificationPanel.addLine("Master player left the game");
    }
    
    @ObservationHandler
    public void onAckedMessageEvent(AckedMessageEvent event) {
        guessButton.setEnabled(true);
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
        PlayerController controller = new PlayerController("player", "master", address, port, "ciao");
        SwingUtilities.invokeLater(new Runnable() { 
            @Override
            public void run() {
                JFrame frame = new GamePlayerFrame(controller);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true); 
            }
        });
        
    }
}