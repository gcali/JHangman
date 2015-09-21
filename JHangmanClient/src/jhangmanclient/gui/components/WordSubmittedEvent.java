package jhangmanclient.gui.components;

import utility.observer.JHEvent;

public class WordSubmittedEvent implements JHEvent {
    
    private String word;

    public WordSubmittedEvent(String word) {
        this.word = word;
    }
    
    public String getWord() {
        return this.word;
    }

}
