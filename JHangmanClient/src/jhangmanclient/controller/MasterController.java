package jhangmanclient.controller;

import java.net.InetAddress;

import utility.Loggable;

public class MasterController extends Loggable {
    
    private String nick;
    private InetAddress address;
    private int port;
    private String key;

    public MasterController(String nick, 
                            InetAddress address, 
                            String key) {
        super("(M) " + nick);
        this.nick = nick;
        this.address = address;
        this.key = key;
        this.printMessage("Address: " + address);
        this.printMessage("Key: " + key);
    }

}
