package jhangmanclient.callback;

import java.rmi.RemoteException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import jhangmanclient.game_data.GameDataInvalidatedEvent;
import jhangmanclient.game_data.GameListViewer;
import jhangmanclient.game_data.GamePlayersChangedEvent;
import jhangmanclient.game_data.NewGameEvent;
import jhangmanclient.game_data.NoGameException;
import jhangmanclient.game_data.RemovedGameEvent;
import rmi_interface.ClientCallbackRMI;
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
    private Map<String, AtomicInteger> gameData = 
            new ConcurrentSkipListMap<String, AtomicInteger>();
    /**
     * ObservableSupport to help handling events
     */
    JHObservableSupport observableSupport = new JHObservableSupport();

    /**
     * {@inheritDoc}
     */
    @Override
    public void addGame(String name) throws RemoteException {
        this.setGamePlayers(name, 0);
        this.observableSupport.publish(new NewGameEvent(name));;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGamePlayers(String name, int number) throws RemoteException {
        AtomicInteger gameValue = this.gameData.get(name);
        if (gameValue == null) {
            this.gameData.put(name, new AtomicInteger(number)); 
        } else {
            gameValue.set(number);
        }
        this.observableSupport.publish(new GamePlayersChangedEvent(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementGamePlayers(String game) throws RemoteException {
        AtomicInteger oldNumber = this.gameData.get(game);
        if (oldNumber != null) {
            oldNumber.incrementAndGet();
        } 
        this.observableSupport.publish(new GamePlayersChangedEvent(game));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void decrementGamePlayers(String game) throws RemoteException {
        AtomicInteger oldNumber = this.gameData.get(game);
        if (oldNumber != null) {
            oldNumber.decrementAndGet();
        } 
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
    public void setGameData(Map<String, Integer> gameList)
        throws RemoteException {
        this.gameData = new ConcurrentSkipListMap<String, AtomicInteger>(); 
        for (Map.Entry<String, Integer> entry : gameList.entrySet()) {
            this.gameData.put(entry.getKey(), 
                              new AtomicInteger(entry.getValue()));
        }
        this.observableSupport.publish(new GameDataInvalidatedEvent());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map.Entry<String, Integer>> getGameList() {
        List<Map.Entry<String, Integer>> gameList = 
                new ArrayList<Map.Entry<String,Integer>>();
        for (Map.Entry<String, AtomicInteger> entry : this.gameData.entrySet()) {
            gameList.add(
                    new AbstractMap.SimpleEntry<String, Integer>(
                        entry.getKey(), entry.getValue().get()
                    )
            );
        }
        return gameList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGamePlayers(String game)
        throws NoGameException {
        AtomicInteger number = this.gameData.get(game);
        if (number == null) {
            throw new NoGameException("No game found: " + game);
        } 
        return number.get();
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
}