package jhangmanclient.udp_interface.player;

import jhangmanclient.udp_interface.MessageID;

public class GuessLetterMessage extends GuessMessage {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final char letter;
    
    public GuessLetterMessage(char letter, String nick) {
        super(MessageID.GUESS_LETTER, nick);
        this.letter = letter;
    }
    
    public char getLetter() {
        return this.letter;
    }
    
}
