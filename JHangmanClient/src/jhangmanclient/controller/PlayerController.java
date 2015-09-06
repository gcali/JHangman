package jhangmanclient.controller;

import java.net.InetAddress;

import utility.Loggable;

public class PlayerController extends Loggable {

    public PlayerController(
        String nick, 
        String gameName, 
        InetAddress address,
        String key
    ) {
        super(nick);
        this.printMessage("Game name: " + gameName);
        this.printMessage("Address: " + address);
        this.printMessage("Key: " + key);
    }

}
