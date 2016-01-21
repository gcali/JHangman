package jhangmanclient.udp_interface.player;

import java.util.UUID;

import jhangmanclient.udp_interface.MessageID;

public class GuessLetterMessage extends GuessMessage {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final char letter;
    
    public GuessLetterMessage(char letter, String nick, UUID uuid) {
        super(MessageID.GUESS_LETTER, nick, uuid);
        this.letter = letter;
    }
    
    public char getLetter() {
        return this.letter;
    }
    
}
