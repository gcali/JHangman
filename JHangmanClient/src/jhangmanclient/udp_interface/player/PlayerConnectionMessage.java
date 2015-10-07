package jhangmanclient.udp_interface.player;

import jhangmanclient.udp_interface.Message;
import jhangmanclient.udp_interface.MessageID;

public class PlayerConnectionMessage extends Message {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String nick;
    private Action action;
    
    public PlayerConnectionMessage(String nick, Action action) {
        super(MessageID.PLAYER_CONNECTION_MESSAGE);
        this.nick = nick;
        this.action = action;
    }
    
    public String getNick() {
        return this.nick;
    }
    
    public Action getAction() {
        return this.action;
    }
    
    public enum Action {
        ABORT, HELLO
    }

}