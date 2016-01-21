package jhangmanclient.controller.master;

import utility.observer.JHEvent;

class InternalLetterGuessedEvent implements JHEvent {

    private boolean[] guessed;
    private String nick;

    public InternalLetterGuessedEvent(boolean[] guessed, String nick) {
        this.guessed = guessed;
        this.nick = nick;
    }
    
    public boolean[] getGuessed() {
        return this.guessed.clone();
    }
    
    public String getNick() {
        return nick;
    }

}
