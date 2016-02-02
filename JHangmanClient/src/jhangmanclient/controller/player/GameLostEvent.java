package jhangmanclient.controller.player;

import utility.observer.JHEvent;

public class GameLostEvent implements JHEvent {
    
    private final String word;

    public GameLostEvent(String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }

}
