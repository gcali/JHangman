package jhangmanclient.controller.player;

import utility.observer.JHEvent;

public class GameOverEvent implements JHEvent {
    
    private final String word;
    private final String winnerNick;

    public GameOverEvent(String word, String winnerNick) {
        this.word = word;
        this.winnerNick = winnerNick;
    }
    
    public String getWord() {
        return this.word;
    }
    
    public String getWinnerNick() {
        return this.winnerNick;
    }

}
