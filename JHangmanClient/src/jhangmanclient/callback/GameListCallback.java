package jhangmanclient.callback;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.concurrent.ConcurrentSkipListMap;

import rmi_interface.ClientCallbackRMI;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Implementation of {@link ClientCallbackRMI}; this implementation
 * adheres to the observer and java bean pattern.
 * 
 * Its addition to the implemented interface are mainly the capability
 * of accessing a {@link List} view of the game data and of adding
 * listeners for its properties.
 * <p />
 * The following
 * are valid properties:
 * <ul>
 *  <li> <b>gameList</b> the list of games, as returned from 
 *      {@link #getGameList()}</li>
 *  <li> <b>gamePlayers</b> the number of players of a game, as returned from
 *      {@link #getGamePlayers(String)}</li>
 * </ul>
 * @author gcali
 *
 */
public class GameListCallback implements ClientCallbackRMI {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    /**
     * The data of the open games; currently, to each game is associated
     * its number of players
     */
    private Map<String, Integer> gameData = 
            new ConcurrentSkipListMap<String, Integer>();
    /**
     * Property support object to facilitate adding listeners
     */
    PropertyChangeSupport propertySupport;

    /**
     * {@inheritDoc}
     */
    @Override
    public void addGame(String name) throws RemoteException {
        this.setGamePlayers(name, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGamePlayers(String name, int number) throws RemoteException {
        Integer oldNumber = this.gameData.get(name);
        SimpleImmutableEntry<String, Integer> oldValue;
        if (oldNumber == null) {
            oldValue = null;
        } else {
            oldValue = new SimpleImmutableEntry<String, Integer>(name, oldNumber);
        }
        SimpleImmutableEntry<String, Integer> newValue = 
                new SimpleImmutableEntry<String, Integer>(name, number);
        this.gameData.put(name, number); 
        this.propertySupport.firePropertyChange("gamePlayers", oldValue, newValue);
    }

    @Override
    public void incrementGamePlayers(String game) throws RemoteException {
        throw new NotImplementedException();
        
    }

    @Override
    public void decrementGamePlayers(String game) throws RemoteException {
        throw new NotImplementedException(); 
    }

    
    /**
     * Returns the number of players for the given {@code game}; if the
     * chosen game isn't open, throws {@link NoGameException}
     * 
     * @param game  the name of the game
     * @return      a tuple, of the form (game, players)
     * @throws NoGameException
     */
    public SimpleImmutableEntry<String, Integer> getGamePlayers(String game)
        throws NoGameException {
        Integer number = this.gameData.get(game);
        if (number == null) {
            throw new NoGameException("No game found: " + game);
        } 
        return new SimpleImmutableEntry<String, Integer>(game, number);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeGame(String name) throws RemoteException {
        Integer number = this.gameData.remove(name);
        if (number != null) {
            this.propertySupport.firePropertyChange(
                    "gameData", 
                    null, 
                    Collections.unmodifiableSet(this.gameData.entrySet())
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGameData(Map<String, Integer> gameList)
        throws RemoteException {
        this.gameData = new ConcurrentSkipListMap<String, Integer>(gameList); 
        this.propertySupport.firePropertyChange(
                "gameList", 
                null, 
                Collections.unmodifiableSet(this.gameData.entrySet())
        );
    }
    
    /**
     * Returns a list view of the open games; the list is made of the tuples
     * (name,players)
     * @return  the list of the games
     */
    public List<Map.Entry<String, Integer>> getGameList() {
        return new ArrayList<Map.Entry<String,Integer>>(
                this.gameData.entrySet()
        );
    }

    /**
     * Adds a new listener for property change events; a list of the properties
     * is available in {@link GameListCallback}
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertySupport.addPropertyChangeListener(listener);
    }

    /**
     * Adds a new {@code listener} for property change events. A list of
     * properties is available in {@link GameListCallback}
     * @param property the property to listen to
     * @param listener the listener that will be notified on property change
     */
    public void addPropertyChangeListener(String property,
                                          PropertyChangeListener listener) {
        this.propertySupport.addPropertyChangeListener(property, listener);
    }

}