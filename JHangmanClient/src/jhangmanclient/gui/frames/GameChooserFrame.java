package jhangmanclient.gui.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jhangmanclient.controller.common.GameChooserController;
import jhangmanclient.controller.common.JoinGameTask;
import jhangmanclient.controller.common.OpenGameTask;
import jhangmanclient.controller.master.GameMasterController;
import jhangmanclient.controller.player.PlayerController;
import jhangmanclient.gui.components.ActionsPanel;
import jhangmanclient.gui.components.AskPositiveNumberDialog;
import jhangmanclient.gui.components.ConfirmGameDialog;
import jhangmanclient.gui.components.GameListTableModel;
import jhangmanclient.gui.utility.SetNickEvent;
import jhangmanclient.gui.utility.Switcher;
import utility.GUIUtils;
import utility.observer.JHObserver;
import utility.observer.ObservationHandler;

public class GameChooserFrame extends HangmanFrame
                              implements JHObserver {
    
    public final static String idString = "gameChooser";
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private GameChooserController gameChooserController;

    private JTable table;

    private JButton joinGameButton;
    private JButton logOutButton;
    private JButton openGameButton;

    private AskPositiveNumberDialog askPlayersDialog;

    private String nick;

    private Switcher switcher;



    public GameChooserFrame(
        GameChooserController gameChooserController,
        String nick,
        Switcher switcher
    ) {
        super(10);
        this.switcher = switcher;
        this.nick = nick;
        setGameController(gameChooserController); 
        
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
    }

    @Override
    protected void initComponents() { 
        JComponent table = initTable();
        this.add(table);

        logOutButton = initLogOutButton(); 
        joinGameButton = initJoinGameButton(); 
        openGameButton = initOpenGameButton();
        
        JButton[] leftButtons = {joinGameButton, openGameButton};
        JButton[] rightButtons = {logOutButton};
        
        JPanel actionsPanel = new ActionsPanel(leftButtons, rightButtons);
        
        this.add(actionsPanel);
    }

    private JButton initOpenGameButton() {
        JButton button = new JButton("Open new game");
        button.addActionListener(new ActionListener() { 
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer players = askForMaxPlayers();
                if (players != null) {
                    openGame(players);
                }
                
            }
        });
        return button;
    }

    private void openGame(int players) {
        OpenGameTask openGameCallable = 
                this.gameChooserController.openGame(players);
        ConfirmGameDialog<GameMasterController> confirmDialog = 
            new ConfirmGameDialog<GameMasterController>(
                this,
                openGameCallable, 
                new Runnable() { 
                    @Override
                    public void run() {
                        openGameCallable.abort();
                        enableInput();
                    } 
                }, 
                new Consumer<GameMasterController>() { 
                    @Override
                    public void accept(GameMasterController t) {
                        switcher.showMaster(GameChooserFrame.this, t);
                        enableInput();
                    }
                
                }
            );
        disableInput();
        confirmDialog.start(); 
    }

    private void joinGame(String name) {
        JoinGameTask joinGameCallable =
            gameChooserController.joinGame(name);
        ConfirmGameDialog<PlayerController> confirmDialog =
            new ConfirmGameDialog<PlayerController>(
                this,
                joinGameCallable,
                new Runnable() { 
                    @Override
                    public void run() {
                        joinGameCallable.abort();
                        enableInput();
                    }
                },
                new Consumer<PlayerController>() {

                    @Override
                    public void accept(PlayerController t) {
                        switcher.showPlayer(GameChooserFrame.this, t);
                        enableInput();
                    }
                }
            );
        disableInput();
        confirmDialog.start();
    }

    private Integer askForMaxPlayers() {
        System.out.println("I'm in max players!");
        this.initDialog();
        System.out.println("Dialog succesfully started");
        Long players = this.askPlayersDialog.getPlayers();
        System.out.println("[GameChooser]" + players);
        if (players == null) {
            return null;
        } else {
            return players.intValue(); 
        }
    }

    private void initDialog() {
        if (this.askPlayersDialog == null) {
            this.askPlayersDialog = 
                    new AskPositiveNumberDialog(
                            this,
                            "How many players for the game?"
                    );
        } 
    }

    private JButton initJoinGameButton() {
        JButton button = new JButton("Join game");
        button.setEnabled(false);
        button.addActionListener( new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                int pos = table.getSelectedRow();
                if (pos >= 0) {
                    String name = (String) table.getValueAt(pos, 0);
                    joinGame(name);
                }
                
            }

        });
        return button;
    }

    private JButton initLogOutButton() {
        JButton button = new JButton("Log out");
        button.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                switcher.showAuth(GameChooserFrame.this);
                try {
                    gameChooserController.handleLogout();
                } catch (RemoteException e1) {
                    showErrorDialog("Couldn't reach the server");
                }
                
            }
        });
        return button;
    }
    
    private void setJoinGameButtonEnabled(boolean b) {
        if (this.joinGameButton != null) {
            this.joinGameButton.setEnabled(b);
        }
    }

    private JComponent initTable() {
        JTable table = new JTable();
        this.table = table;
        JScrollPane pane = new JScrollPane(
                table, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        return pane;
    }
    
    public void setGameController(GameChooserController controller) {
        this.gameChooserController = controller;
        this.table.setModel(new GameListTableModel(controller.getViewer()));
        this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.table.getSelectionModel().addListSelectionListener(
            new TableSelectionListener(
                this.table,
                b -> setJoinGameButtonEnabled(b)
        ));
        this.table.getColumnModel().getColumn(1).setMaxWidth(50);
    } 
    
    private void disableInput() {
        GUIUtils.invokeAndWait(() -> {
            logOutButton.setEnabled(false);
            joinGameButton.setEnabled(false);
            openGameButton.setEnabled(false);
        });
    }
    
    private void enableInput() {
        GUIUtils.invokeAndWait(() -> {
            logOutButton.setEnabled(true);
            joinGameButton.setEnabled(true);
            openGameButton.setEnabled(true);
        });
    }
    
    @ObservationHandler
    public void onSetNickEvent(SetNickEvent e) {
        System.out.println("Hi!");
        this.nick = e.getNick();
        this.updateTitle();
    }
    
    @Override
    protected String getHangmanTitle() {
        return super.getHangmanTitle() + " - " + this.nick;
    }
    
    private class TableSelectionListener implements ListSelectionListener {
        private JTable table;
        private Consumer<Boolean> setButtonVisibility;

        public TableSelectionListener(JTable table,
                                      Consumer<Boolean> setButtonVisibility) {
            this.table = table;
            this.setButtonVisibility = setButtonVisibility;
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                this.setButtonVisibility.accept(this.table.getSelectedRow() >= 0); 
            }
        }
    }
    @Override
    public String getLoggableId() {
        return "GameChooserFrame";
    }
}
