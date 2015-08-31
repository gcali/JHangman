package jhangmanserver.game_data;

import java.util.HashSet;
import java.util.Set;


public class ServerGameData {

    private String name;
    private int maxPlayers;
    private Set<String> players;

    public ServerGameData(String name, int maxPlayers) {
        this.name = name;
        this.maxPlayers = maxPlayers;
        this.players = new HashSet<String>();
    }
    
    /**
     * Adds a new player to the game
     * @param nick the nick of the player to be added
     * @return {@code true} if the game is complete, {@code false} otherwise
     */
    public synchronized boolean addPlayer(String nick) {
        this.players.add(nick);
        if (this.players.size() == this.maxPlayers) {
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
    
}
