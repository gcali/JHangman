package jhangmanclient.gui.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import jhangmanclient.controller.GameChooserController;
import jhangmanclient.controller.MasterController;
import jhangmanclient.gui.components.ActionsPanel;
import jhangmanclient.gui.components.AskPositiveNumberDialog;
import jhangmanclient.gui.components.GameListTableModel;
import jhangmanclient.gui.utility.Changer;

public class GameChooserFrame extends HangmanFrame {
    
    public final static String idString = "gameChooser";
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    Changer changer = null;
    private GameChooserController gameChooserController;

    private JTable table;

    private JButton joinGameButton;

    private AskPositiveNumberDialog askPlayersDialog;

    public GameChooserFrame(Changer changer) {
        super(10);
        this.changer = changer;
        this.setVisible(false);
    }
    
    @Override
    protected void initComponents() { 
        JComponent table = initTable();
        this.add(table);

        JButton logOutButton = initLogOutButton(); 
        
        JButton joinGameButton = initJoinGameButton();
        
        JButton openGameButton = initOpenGameButton();
        
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
        Future<MasterController> futureResult = 
                this.gameChooserController.openGame(players);
        Thread thread = new Thread() {
            @Override
            public void run() {
                MasterController controller = null;
                System.out.println("Opening game...");
                try {
                    controller = futureResult.get();
                } catch (InterruptedException e) {
                    System.err.println("I got interrupted!");
                    return;
                } catch (ExecutionException e) {
                    System.err.println("Execution exception, damn");
                    e.printStackTrace();
                    return;
                }
                if (controller != null) {
                    System.out.println("Game open!"); 
                } else {
                    System.out.println("Game not open...");
                }
            }
        };
        thread.start();
        
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
//            this.askPlayersDialog.setVisible(false);
//            this.askPlayersDialog.setModalityType(
//                    ModalityType.APPLICATION_MODAL
//            );
        } 
    }

    private JButton initJoinGameButton() {
        JButton button = new JButton("Join game");
        button.setEnabled(false);
        this.joinGameButton = button;
        return button;
    }

    private JButton initLogOutButton() {
        JButton button = new JButton("Log out");
        button.addActionListener(e -> this.changer.changeFrame("auth"));
        button.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
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
        this.table.getColumnModel().getColumn(1).setMaxWidth(50);
    } 
}
