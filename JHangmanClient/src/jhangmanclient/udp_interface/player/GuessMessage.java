package jhangmanclient.udp_interface.player;

import java.util.UUID;

import jhangmanclient.udp_interface.Message;
import jhangmanclient.udp_interface.MessageID;

public abstract class GuessMessage extends Message {


    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public GuessMessage(MessageID id, String nick, UUID uuid) {
        super(id, nick, uuid);
    }
    
}
