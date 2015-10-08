package jhangmanclient.udp_interface.player;

import jhangmanclient.udp_interface.Message;
import jhangmanclient.udp_interface.MessageID;

public abstract class GuessMessage extends Message {


    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String nick;
    
    public GuessMessage(MessageID id, String nick) {
        super(id);
        this.nick = nick;
    }
    
    public String getNick() {
        return this.nick;
    }
}
