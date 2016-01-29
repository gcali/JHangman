package jhangmanclient.udp_interface.master;

import java.util.UUID;

import jhangmanclient.udp_interface.Message;
import jhangmanclient.udp_interface.MessageID;

public class GameUpdateMessage extends Message {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final String visibleWord;
    private final boolean isOver;
    private final String winnerNick;
    private final int remainingLives;
    private final int sequenceNumber;
    private final int maxLives;
    
    public GameUpdateMessage(
        int sequenceNumber,
        String visibleWord, 
        int remainingLives,
        int maxLives,
        boolean isOver, 
        String winnerNick,
        String ackNick,
        UUID uuid
    ) {
        super(MessageID.GAME_UPDATE_MESSAGE, ackNick, uuid);
        this.sequenceNumber = sequenceNumber;
        this.visibleWord = visibleWord;
        this.remainingLives = remainingLives;
        this.maxLives = maxLives;
        this.isOver = isOver;
        this.winnerNick = winnerNick;
    } 
    
    public String getVisibleWord() {
        return this.visibleWord;
    }
    
    public int getRemainingLives() {
        return this.remainingLives;
    }

    public boolean isOver() {
        return this.isOver;
    }

    public String getWinnerNick() {
        return winnerNick;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public int getMaxLives() {
        return maxLives;
    }
}