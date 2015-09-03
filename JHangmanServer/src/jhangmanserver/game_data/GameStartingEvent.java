package jhangmanserver.game_data;

import java.net.InetAddress;

import utility.observer.JHEvent;

public class GameStartingEvent implements JHEvent {
    
    private final String key;
    private final InetAddress address;

    public GameStartingEvent(String key, InetAddress address) {
        this.key = key;
        this.address = address;
    }
    
    public String getKey() {
        return this.key;
    }
    
    public InetAddress getAddress() {
        return this.address;
    } 
}