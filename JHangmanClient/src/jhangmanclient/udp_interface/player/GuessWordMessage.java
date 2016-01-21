package jhangmanclient.udp_interface.player;

import java.util.UUID;

import jhangmanclient.udp_interface.MessageID;

public class GuessWordMessage extends GuessMessage {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String word;
    
    public GuessWordMessage(String word, String nick, UUID uuid) {
        super(MessageID.GUESS_WORD, nick, uuid);
        this.word = word;
    }
    
    public String getWord() {
        return this.word;
    }

}
