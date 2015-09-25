package jhangmanclient.controller.master;

import utility.observer.JHEvent;

public class ConnectedPlayerEvent implements JHEvent {
    
    private final String nick;

    public ConnectedPlayerEvent(String nick) {
        this.nick = nick;
    }
    
    public String getNick() {
        return this.nick;
    }

}
