package jhangmanserver.game_data;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import utility.Cleaner;
import utility.Loggable;
import utility.observer.JHObservable;
import utility.observer.JHObservableSupport;
import utility.observer.JHObserver;


/**
 * Events: 
 *  <ul>
 *      <li>{@link GameFullEvent}</li>
 *      <li>{@link AbortedGameEvent}</li>
 *      <li>{@link GameStartingEvent}</li>
 *  </ul>
 * @author gcali
 *
 */
public class ServerGameData implements Loggable, JHObservable {

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
     * @throws PlayerAlreadyJoinedException 
     */
    public synchronized Cleaner addPlayer(String nick, 
                                          JHObserver fullGameObserver) 
            throws GameFullException, 
                   PlayerAlreadyJoinedException,
                   PlayerIsMasterException {
        if (this.players.size() == this.maxPlayers) {
            throw new GameFullException();
        }
        if (this.name.equals(nick)) {
            throw new PlayerIsMasterException();
        }
        boolean newPlayer = this.players.add(nick);
        printDebugMessage(nick + " newPlayer: " + newPlayer);
        if (!newPlayer) {
            throw new PlayerAlreadyJoinedException();
        }
        if (fullGameObserver != null) {
            printDebugMessage("Adding observer...");
            this.addObserver(fullGameObserver); 
        }
        if (this.players.size() == this.maxPlayers) {
            this.observableSupport.publish(new GameFullEvent());
        }
        
        return new Cleaner() { 
            @Override
            public void cleanUp() {
                ServerGameData.this.observableSupport.remove(fullGameObserver);
            }
        };
    }
    
    public synchronized boolean removePlayer(String nick) {
        boolean done = this.players.remove(nick);
        if (done) {
            this.observableSupport.publish(new PlayerLeftEvent(nick));
        }
        return done;
    }
    
    public synchronized boolean isPlayerIn(String nick) {
        return this.players.contains(nick);
    }
    
    public synchronized String getName() {
        return this.name;
    }
    
    public synchronized Set<String> getPlayers() {
        return new HashSet<String>(this.players);
    }
    
    public synchronized int getCurrentPlayers() {
        return this.players.size();
    }
    
    public synchronized int getMaxPlayers() {
        return this.maxPlayers;
    }

    @Override
    public void addObserver(JHObserver observer) {
        printDebugMessage("From " + this + ", Observer added: " + observer.toString());
        this.observableSupport.add(observer); 
    }

    @Override
    public void removeObserver(JHObserver observer) {
        this.observableSupport.remove(observer); 
    }

    public void abortGame() {
        printDebugMessage("From " + this + ", abort sent");
        this.observableSupport.publish(new AbortedGameEvent());
    }
    
    public void setKeyAddressPort(String key, InetAddress address, int port) {
        printDebugMessage("Throwing GameStartingEvent");
        this.observableSupport.publish(new GameStartingEvent(key, address, port));
    }

    @Override
    public String getLoggableId() {
        return "GameData";
    }
    
}
