package udp_interface.master;

import udp_interface.Message;
import udp_interface.MessageID;

public class MasterHandshakeMessage extends Message {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final String word;

    public MasterHandshakeMessage(String word) {
        super(MessageID.MASTER_HANDSHAKE);
        this.word = word;
    }
    
    public String getWord() {
        return this.word;
    }
    
}
