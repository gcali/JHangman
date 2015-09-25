package jhangmanclient.controller.player;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

import utility.Loggable;

public class PlayerController implements Loggable, Closeable {

    private String nick;
    private InetAddress address;
    private String key;
    private MulticastSocket socket = null;

    public PlayerController(
        String nick, 
        String gameName, 
        InetAddress address,
        String key
    ) {
        this.nick = nick;
        this.address = address;
        this.key = key;
        this.printDebugMessage("Game name: " + gameName);
        this.printDebugMessage("Address: " + address);
        this.printDebugMessage("Key: " + key);
    }
    
    public void initConnection() throws IOException {
        this.socket = new MulticastSocket();
        this.socket.joinGroup(this.address);
    }

    @Override
    public String getLoggableId() {
        return this.nick;
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub
        
    }

}