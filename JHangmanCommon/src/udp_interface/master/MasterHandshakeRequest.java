package udp_interface.master;

import udp_interface.Message;
import udp_interface.MessageID;

public class MasterHandshakeRequest extends Message {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final String nick;
    
    public MasterHandshakeRequest(String nick) {
        super(MessageID.MASTER_HANDSHAKE_REQUEST);
        this.nick = nick;
    }
    
    public String getNick() {
        return this.nick;
    }

}
