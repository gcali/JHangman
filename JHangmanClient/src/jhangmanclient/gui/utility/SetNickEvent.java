package jhangmanclient.gui.utility;

import utility.observer.JHEvent;

public class SetNickEvent implements JHEvent {
    
    private String nick;

    public SetNickEvent(String nick) {
        this.nick = nick;
    }
    
    public String getNick() {
        return this.nick;
    }

}
