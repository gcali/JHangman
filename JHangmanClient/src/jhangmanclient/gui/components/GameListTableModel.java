package jhangmanclient.gui.components;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import jhangmanclient.game_data.GameDataInvalidatedEvent;
import jhangmanclient.game_data.GameListViewer;
import jhangmanclient.game_data.GamePlayerStatus;
import jhangmanclient.game_data.GamePlayersChangedEvent;
import jhangmanclient.game_data.NewGameEvent;
import jhangmanclient.game_data.NoGameException;
import jhangmanclient.game_data.RemovedGameEvent;
import rmi_interface.SingleGameData;
import utility.observer.JHObserver;
import utility.observer.ObservationHandler;

public class GameListTableModel 
    extends AbstractTableModel 
    implements JHObserver {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private GameListViewer viewer;
    private List<SingleGameData> gameData;
    
    private static final String[] columnNames = {"Name", "Players"};

    public GameListTableModel(GameListViewer viewer) {
        this.initViewerObserving(viewer);
    }

    private synchronized void initViewerObserving(GameListViewer viewer) { 
        this.viewer = viewer;
        viewer.addObserver(this);
        this.gameData = viewer.getGameList();
    }
    
    @ObservationHandler
    public synchronized void onNewGameEvent(NewGameEvent e) {
        this.gameData.add(
                new SingleGameData(e.getName(), e.getMaxPlayers(), 0)
        );
        int pos = this.gameData.size() - 1;
        this.fireTableRowsInserted(pos, pos);
    }
    
    @ObservationHandler
    public synchronized void onRemovedGameEvent(RemovedGameEvent e) {
        Integer pos = findElementPos(this.gameData, e.getName());
        System.out.println("[Model] Removing element " + pos);
        if (pos != null) {
            this.gameData.remove(pos.intValue());
            this.fireTableRowsDeleted(pos, pos); 
            System.out.println("[Model] Removed");
        } else {
            System.err.println("Couldn't find game " + 
                               e.getName() + 
                               " in table view");
        }
    }
    
    private static synchronized Integer findElementPos(
            List<SingleGameData> gameData, 
            String name
    ) {
        int pos = 0;
        for (SingleGameData entry : gameData) {
            if (name.equals(entry.getName())) {
                return pos;
            }
            pos++;
        }
        return null;
    }

    @ObservationHandler
    public synchronized void onGamePlayersChangedEvent(GamePlayersChangedEvent e) {
        Integer pos = findElementPos(this.gameData, e.getName());
        if (pos != null) {
            try {
                SingleGameData updatedData = 
                        this.viewer.getSingleGameData(e.getName());
                this.gameData.set(pos, updatedData);
                this.fireTableRowsUpdated(pos, pos);
            } catch (NoGameException e1) {
                e1.printStackTrace();
            }
        }
    }
    
    @ObservationHandler
    public synchronized void onGameDataInvalidatedEvent(GameDataInvalidatedEvent e) {
        this.gameData = this.viewer.getGameList();
        this.fireTableDataChanged();
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    };

    @Override
    public synchronized int getRowCount() {
        return this.gameData.size();
    }

    @Override
    public synchronized int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public synchronized Object getValueAt(int rowIndex, int columnIndex) {
        SingleGameData element = this.gameData.get(rowIndex);
        switch (columnIndex) {
        case 0:
            return element.getName();
        case 1:
            return new GamePlayerStatus(element.getCurrentPlayers(), 
                                        element.getMaxPlayers());
        default:
            throw new IndexOutOfBoundsException();
        }
    }
    
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    } 
}