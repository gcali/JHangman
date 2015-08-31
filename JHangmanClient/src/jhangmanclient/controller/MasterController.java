package jhangmanclient.controller;

import java.net.InetAddress;

public class MasterController {
    
    private String nick;
    private InetAddress address;
    private int port;
    private String key;

    public MasterController(String nick, 
                            InetAddress address, 
                            int port,
                            String key) {
        this.nick = nick;
        this.address = address;
        this.port = port;
        this.key = key;
    }

}
