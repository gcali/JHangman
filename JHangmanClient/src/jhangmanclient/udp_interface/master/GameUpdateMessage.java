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
    private final GameStatus status;
    private final String winnerNick;
    private final int remainingLives;
    private final int sequenceNumber;
    private final int maxLives;
    
    public GameUpdateMessage(
        int sequenceNumber,
        String visibleWord, 
        int remainingLives,
        int maxLives,
        GameStatus status, 
        String winnerNick,
        String ackNick,
        UUID uuid
    ) {
        super(MessageID.GAME_UPDATE_MESSAGE, ackNick, uuid);
        this.sequenceNumber = sequenceNumber;
        this.visibleWord = visibleWord;
        this.remainingLives = remainingLives;
        this.maxLives = maxLives;
        this.status = status;
        this.winnerNick = winnerNick;
    } 
    
    public String getVisibleWord() {
        return this.visibleWord;
    }
    
    public int getRemainingLives() {
        return this.remainingLives;
    }

    public GameStatus getStatus() {
        return this.status;
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
    
    public String toString() {
    
        StringBuilder builder = new StringBuilder();
        
        builder.append(visibleWord);
        builder.append(" ");
        builder.append(status);
        builder.append(" ");
        builder.append(winnerNick);
        builder.append(" ");
        builder.append(getNick());
        
        return builder.toString();
        
    }
    
    public enum GameStatus {
        PLAYING, WON, LOST, ABORTED
    }
}