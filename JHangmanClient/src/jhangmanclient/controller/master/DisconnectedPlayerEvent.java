package jhangmanclient.controller.master;

import utility.observer.JHEvent;

public class DisconnectedPlayerEvent implements JHEvent {
    
    private final String nick;
    
    public DisconnectedPlayerEvent(String nick) {
        this.nick = nick;
    }
    
    public String getNick() {
        return this.nick;
    }

}
