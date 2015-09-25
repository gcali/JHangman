package jhangmanclient.controller.master;

import utility.observer.JHEvent;

public class LetterGuessedEvent implements JHEvent {

    private String visibleWord;

    public LetterGuessedEvent(String visibleWord) {
        this.visibleWord = visibleWord;
    }
    
    public String getVisibileWord() {
        return this.visibleWord;
    }

}