package jhangmanclient.udp_interface.player;

import jhangmanclient.udp_interface.MessageID;

public class GuessWordMessage extends GuessMessage {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String word;
    
    public GuessWordMessage(String word, String nick) {
        super(MessageID.GUESS_WORD, nick);
        this.word = word;
    }
    
    public String getWord() {
        return this.word;
    }

}
