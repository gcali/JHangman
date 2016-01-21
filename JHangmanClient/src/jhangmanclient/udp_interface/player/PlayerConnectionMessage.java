package jhangmanclient.udp_interface.player;

import java.util.UUID;

import jhangmanclient.udp_interface.Message;
import jhangmanclient.udp_interface.MessageID;

public class PlayerConnectionMessage extends Message {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Action action;
    
    public PlayerConnectionMessage(String nick, Action action, UUID uuid) {
        super(MessageID.PLAYER_CONNECTION_MESSAGE, nick, uuid);
        this.action = action;
    }
    
    public Action getAction() {
        return this.action;
    }
    
    public enum Action {
        ABORT, HELLO
    }

}