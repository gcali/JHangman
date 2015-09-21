package udp_interface.player;

import udp_interface.Message;
import udp_interface.MessageID;

public class GuessMessage extends Message {


    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final String guess;
    private GuessWordOrLetter category;
    private String nick;
    
    
    
    public GuessMessage(GuessWordOrLetter category, String guess, String nick) {
        super(MessageID.GUESS);
        this.category = category;
        this.guess = guess;
        this.nick = nick;
    }
    
    public String getGuess() {
        return this.guess;
    }
    
    public String getNick() {
        return this.nick;
    }
    
    public GuessWordOrLetter getCategory() {
        return this.category;
    }

    
    public enum GuessWordOrLetter {
        GUESS_WORD, GUESS_LETTER;
    }
}
