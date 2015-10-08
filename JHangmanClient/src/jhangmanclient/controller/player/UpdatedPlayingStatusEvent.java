package jhangmanclient.controller.player;

import utility.observer.JHEvent;

public class UpdatedPlayingStatusEvent implements JHEvent {
    
    private final String word;
    private int lives;
    
    public UpdatedPlayingStatusEvent(String word, int lives) {
        this.word = word;
        this.lives = lives;
    }
    
    public String getWord() {
        return this.word;
    }
    
    public int getLives() {
        return this.lives;
    }

}
