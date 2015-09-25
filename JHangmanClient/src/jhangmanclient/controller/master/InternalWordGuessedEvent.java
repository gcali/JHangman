package jhangmanclient.controller.master;

import utility.observer.JHEvent;

class InternalWordGuessedEvent implements JHEvent {
    
    private final String winnerNick;
    
    public InternalWordGuessedEvent(String winnerNick) {
        this.winnerNick = winnerNick;
    }
    
    public String getWinnerNick() {
        return this.winnerNick;
    }

}
