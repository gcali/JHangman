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
        int port,
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
        return "(P) " + this.nick;
    }

    @Override
    public void close() {
        if (this.socket != null) {
            try {
                this.socket.leaveGroup(this.address);
            } catch (IOException e) {
                this.printError("Couldn't leave multicast group, ignoring");
            }
            this.socket.close();
            this.socket = null;
        }
    }
    
    public static void main(String[] args) throws IOException {
        String nick = null;
        String gameName = "Master";
        String key = "ciao";
        String addressArg = "239.255.54.67";
        String portArg = "49312";
        String word = "ciao";
        
        nick = args[0];
        
        try {
            int i = 1;
            key = args[i++];
            word = args[i++]; 
            gameName = args[i++];
            addressArg = args[i++];
            portArg = args[i++];
        } catch (ArrayIndexOutOfBoundsException e) { 
        }
        
        InetAddress address = InetAddress.getByName(addressArg);
        int port = Integer.parseInt(portArg);
        
        PlayerController controller = new PlayerController(
            nick, 
            gameName, 
            address, 
            port, 
        key);
        
        controller.initConnection();
        
        for (int i=0; i < 5; i++) {
            
        }
    } 
}