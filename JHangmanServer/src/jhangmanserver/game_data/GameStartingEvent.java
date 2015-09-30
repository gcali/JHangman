package jhangmanserver.game_data;

import java.net.InetAddress;

import utility.observer.JHEvent;

public class GameStartingEvent implements JHEvent {
    
    private final String key;
    private final InetAddress address;
    private int port;

    public GameStartingEvent(String key, InetAddress address, int port) {
        this.key = key;
        this.address = address;
        this.port = port;
    }
    
    public String getKey() {
        return this.key;
    }
    
    public InetAddress getAddress() {
        return this.address;
    } 
    
    public int getPort() {
        return this.port;
    }
}