package jhangmanclient.controller.master;

import utility.observer.JHEvent;

public class WordGuessedEvent implements JHEvent {
    
    private final String word;
    private final String winnerNick;

    public WordGuessedEvent(String word, String winnerNick) {
        this.word = word;
        this.winnerNick = winnerNick;
    }

    public String getWord() {
        return word;
    }

    public String getWinnerNick() {
        return winnerNick;
    } 
}
