package jhangmanclient.controller.master;

import utility.observer.JHEvent;

class InternalLetterGuessedEvent implements JHEvent {

    private boolean[] guessed;

    public InternalLetterGuessedEvent(boolean[] guessed) {
        this.guessed = guessed;
    }
    
    public boolean[] getGuessed() {
        return this.guessed.clone();
    }

}
