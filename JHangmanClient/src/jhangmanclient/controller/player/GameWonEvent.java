package jhangmanclient.controller.player;

import utility.observer.JHEvent;

public class GameWonEvent implements JHEvent {

    private final String visibleWord;
    private final String winnerNick;

    public GameWonEvent(String visibleWord, String winnerNick) {
        this.visibleWord = visibleWord;
        this.winnerNick = winnerNick;
    }

    public String getVisibleWord() {
        return visibleWord;
    }

    public String getWinnerNick() {
        return winnerNick;
    }
    
}
