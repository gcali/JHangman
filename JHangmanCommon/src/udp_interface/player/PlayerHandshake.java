package udp_interface.player;

import udp_interface.Message;
import udp_interface.MessageID;

public class PlayerHandshake extends Message {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String nick;
    
    public PlayerHandshake(String nick) {
        super(MessageID.PLAYER_HANDSHAKE);
        this.nick = nick;
    }
    
    public String getNick() {
        return this.nick;
    }

}