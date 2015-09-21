package jhangmanclient.controller;

import java.net.InetAddress;

import utility.Loggable;

public class PlayerController implements Loggable {

    private String nick;

    public PlayerController(
        String nick, 
        String gameName, 
        InetAddress address,
        String key
    ) {
        this.nick = nick;
        this.printDebugMessage("Game name: " + gameName);
        this.printDebugMessage("Address: " + address);
        this.printDebugMessage("Key: " + key);
    }

    @Override
    public String getId() {
        return this.nick;
    }

}
