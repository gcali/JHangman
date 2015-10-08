package jhangmanclient.udp_interface.master;

import jhangmanclient.udp_interface.Message;
import jhangmanclient.udp_interface.MessageID;

@Deprecated
public class MasterHelloMessage extends Message {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final String word;

    public MasterHelloMessage(String word) {
        super(MessageID.MASTER_HELLO);
        this.word = word;
    }
    
    public String getWord() {
        return this.word;
    }
    
}
