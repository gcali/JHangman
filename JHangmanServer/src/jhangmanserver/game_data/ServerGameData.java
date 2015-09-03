package jhangmanserver.game_data;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import jhangmanserver.remote.GameFullEvent;
import utility.observer.JHObservable;
import utility.observer.JHObservableSupport;
import utility.observer.JHObserver;


/**
 * Events: 
 *  <ul>
 *      <li>{@link GameFullEvent}</li>
 *      <li>{@link AbortedGameEvent}</li>
 *  </ul>
 * @author gcali
 *
 */
public class ServerGameData implements JHObservable {

    private JHObservableSupport observableSupport = new JHObservableSupport();
    private String name;
    private int maxPlayers;
    private Set<String> players;

    public ServerGameData(String name, int maxPlayers) {
        this.name = name;
        this.maxPlayers = maxPlayers;
        this.players = new HashSet<String>();
    }
    
    /**
     * Adds a new player to the game, registering an observer interested in
     * {@link GameFullEvent}
     * @param nick the nick of the player to be added
     * @param fullGameObserver the observer to add; if {@code null}, no observer
     *                         is registered
     * @return {@code true} if the game is complete, {@code false} otherwise
     */
    public synchronized boolean addPlayer(String nick, 
                                          JHObserver fullGameObserver) 
            throws GameFullException {
        if (this.players.size() == this.maxPlayers) {
            throw new GameFullException();
        }
        this.players.add(nick);
        if (fullGameObserver != null) {
            this.addObserver(fullGameObserver); 
        }
        if (this.players.size() == this.maxPlayers) {
            this.observableSupport.publish(new GameFullEvent());
            return true;
        } else {
            return false;
        }
    }
    
    public synchronized void removePlayer(String nick) {
        this.players.remove(nick);
    }
    
    public synchronized String getName() {
        return this.name;
    }
    
    public synchronized Set<String> getPlayers() {
        return new HashSet<String>(this.players);
    }

    @Override
    public void addObserver(JHObserver observer) {
        this.observableSupport.add(observer); 
    }

    @Override
    public void removeObserver(JHObserver observer) {
        this.observableSupport.remove(observer); 
    }

    public void abortGame() {
        this.observableSupport.publish(new AbortedGameEvent());
    }
    
    public void setKeyAddress(String key, InetAddress address) {
        this.observableSupport.publish(new GameStartingEvent(key, address));
    }
    
}
