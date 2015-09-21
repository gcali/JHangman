package udp_interface.master;

import udp_interface.Message;
import udp_interface.MessageID;

public class GameUpdateMessage extends Message {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final int sequenceNumber;
    private final String visibleWord;
    private final boolean isOver;
    private final String winnerNick;
    
    public GameUpdateMessage(
        int sequenceNumber,
        String visibleWord, 
        boolean isOver, 
        String winnerNick
    ) {
        super(MessageID.GAME_UPDATE_MESSAGE);
        this.sequenceNumber = sequenceNumber;
        this.visibleWord = visibleWord;
        this.isOver = isOver;
        this.winnerNick = winnerNick;
    } 
}