package jhangmanclient.gui.frames;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import jhangmanclient.gui.components.ActionsPanel;
import jhangmanclient.gui.components.LivesIndicatorPanel;
import jhangmanclient.gui.components.NotificationPanel;
import jhangmanclient.gui.components.WordInputPanel;
import jhangmanclient.gui.components.WordPlayerDisplay;
import utility.Loggable;
import utility.observer.JHObserver;

public class GameMasterFrame extends HangmanFrame 
                             implements JHObserver,
                                        Loggable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private GameMasterController controller;

    private final Runnable activateFrameAfterAbort;

    private NotificationPanel notificationPanel;

    private LivesIndicatorPanel livesPanel;

    private JButton submitButton;

    private JButton abortButton;

    private WordInputPanel inputPanel;

    private WordPlayerDisplay wordPanel;

    public GameMasterFrame(
        GameMasterController controller, 
        Runnable activateFrameAfterAbort
    ) {
        super();
        this.controller = controller;
        this.activateFrameAfterAbort = activateFrameAfterAbort;
        setUpActions();
        terminateInitialization();
    }
    
    private void setUpActions() {
        submitButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        });
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
        abortButton = new JButton("Abort");
        
        JButton [] leftButtons = {abortButton};
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
            this.showGame();
        } catch (IOException e1) {
            this.printError("Couldn't init controller, should retry");
        }
    }
    
    private void showGame() {
        // TODO Auto-generated method stub
        
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
        this.activateFrameAfterAbort.run();
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
        GameMasterController controller = new GameMasterController("master", address, port, "ciao", 5);
        System.out.println(controller);
        //TODO stub
        JFrame frame = new GameMasterFrame(controller, () -> {});
        frame.setVisible(true);
    }
}