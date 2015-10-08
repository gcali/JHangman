package jhangmanclient.udp_interface.master;

import jhangmanclient.udp_interface.Message;
import jhangmanclient.udp_interface.MessageID;

public class GameUpdateMessage extends Message {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final int sequenceNumber;
    private final String visibleWord;
    private final boolean isOver;
    private final String winnerNick;
    private int lives;
    
    public GameUpdateMessage(
        int sequenceNumber,
        String visibleWord, 
        int lives,
        boolean isOver, 
        String winnerNick
    ) {
        super(MessageID.GAME_UPDATE_MESSAGE);
        this.sequenceNumber = sequenceNumber;
        this.visibleWord = visibleWord;
        this.lives = lives;
        this.isOver = isOver;
        this.winnerNick = winnerNick;
    } 
    
    public int getSequenceNumber() {
        return this.sequenceNumber;
    }

    public String getVisibleWord() {
        return this.visibleWord;
    }
    
    public int getLives() {
        return this.lives;
    }

    public boolean isOver() {
        return this.isOver;
    }

    public String getWinnerNick() {
        return winnerNick;
    }
}