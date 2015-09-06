package jhangmanserver.game_data;

import utility.observer.JHEvent;

public class PlayerLeftEvent implements JHEvent {
    
    private String nick;

    public PlayerLeftEvent(String nick) {
        this.nick = nick;
    }

    public String getNick() {
        return nick;
    }

}
