package jhangmanclient.controller.player;

import utility.observer.JHEvent;

public class UpdatedPlayingStatusEvent implements JHEvent {
    
    private final String word;
    private final int remainingLives;
    private final int maxLives;
    
    public UpdatedPlayingStatusEvent(String word, 
        int remainingLives, int maxLives) {
        this.word = word;
        this.remainingLives = remainingLives;
        this.maxLives = maxLives;
    }
    
    public String getWord() {
        return this.word;
    }
    
    public int getRemainingLives() {
        return this.remainingLives;
    }

    public int getMaxLives() {
        return maxLives;
    }

}
