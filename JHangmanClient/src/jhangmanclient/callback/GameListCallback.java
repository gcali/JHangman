package jhangmanclient.callback;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import jhangmanclient.game_data.GameDataInvalidatedEvent;
import jhangmanclient.game_data.GameListViewer;
import jhangmanclient.game_data.GamePlayersChangedEvent;
import jhangmanclient.game_data.NewGameEvent;
import jhangmanclient.game_data.NoGameException;
import jhangmanclient.game_data.RemovedGameEvent;
import rmi_interface.ClientCallbackRMI;
import rmi_interface.SingleGameData;
import utility.observer.JHObservable;
import utility.observer.JHObservableSupport;
import utility.observer.JHObserver;

/**
 * Implementation of {@link ClientCallbackRMI}; this implementation adheres
 * to the observer pattern, via the {@link JHObservable} interface.
 * <p />
 * It exposes its data through the methods of the interface 
 * {@link GameListViewer}
 * 
 * @author gcali
 *
 */
public class GameListCallback implements ClientCallbackRMI, 
                                         GameListViewer {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    /**
     * The data of the open games; currently, to each game is associated
     * its number of players
     */
    private Map<String, GamePlayerDataAtomic> gameData = 
            new ConcurrentSkipListMap<String, GamePlayerDataAtomic>();
    /**
     * ObservableSupport to help handling events
     */
    JHObservableSupport observableSupport = new JHObservableSupport();

    /**
     * {@inheritDoc}
     */
    @Override
    public void addGame(String name, int maxPlayers) throws RemoteException {
        this.gameData.put(name, new GamePlayerDataAtomic(name, maxPlayers));
        this.observableSupport.publish(new NewGameEvent(name, maxPlayers));;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setGamePlayers(String name, int number) throws RemoteException {
        this.gameData.get(name).setCurrentPlayers(number);
        this.observableSupport.publish(new GamePlayersChangedEvent(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementGamePlayers(String game) throws RemoteException {
        this.gameData.get(game).incrementCurrentPlayers();
        this.observableSupport.publish(new GamePlayersChangedEvent(game));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void decrementGamePlayers(String game) throws RemoteException {
        this.gameData.get(game).decrementCurrentPlayers();
        this.observableSupport.publish(new GamePlayersChangedEvent(game));
    }

    

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeGame(String name) throws RemoteException {
        this.gameData.remove(name);
        this.observableSupport.publish(new RemovedGameEvent(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGameData(Map<String, SingleGameData> gameList)
        throws RemoteException {
        this.gameData = 
                new ConcurrentSkipListMap<String, GamePlayerDataAtomic>(); 
        for (Map.Entry<String, SingleGameData> entry : gameList.entrySet()) {
            SingleGameData data = entry.getValue();
            this.gameData.put(entry.getKey(), 
                              new GamePlayerDataAtomic(
                                      entry.getKey(), 
                                      data.getMaxPlayers(), 
                                      data.getCurrentPlayers()
                              ));
        }
        this.observableSupport.publish(new GameDataInvalidatedEvent());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<SingleGameData> getGameList() {
        List<SingleGameData> gameList = 
                new ArrayList<SingleGameData>();
        for (GamePlayerDataAtomic entry : this.gameData.values()) {
            gameList.add(
                    new SingleGameData(entry.getName(), 
                                       entry.getMaxPlayers(), 
                                       entry.getCurrentPlayers())
            );
        }
        return gameList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SingleGameData getSingleGameData(String game) 
            throws NoGameException {
        GamePlayerDataAtomic data = this.gameData.get(game);
        if (data == null) {
            throw new NoGameException("Game not found: " + game);
        }
        return new SingleGameData(
                game, 
                data.getMaxPlayers(), 
                data.getCurrentPlayers()
        );
    } 

    /**
     * {@inheritDoc}
     * 
     * For the observable events, look at {@link GameListViewer}
     */
    @Override
    public void addObserver(JHObserver observer) {
        this.observableSupport.add(observer); 
    }

    @Override
    public void removeObserver(JHObserver observer) {
        this.observableSupport.remove(observer); 
    }

}