package jhangmanclient.controller.master;

import java.util.UUID;

import utility.observer.JHEvent;

public class SendUpdateEvent implements JHEvent {
    
    private String nick;
    private UUID uuid;

    public SendUpdateEvent(String nick, UUID uuid) {
        this.nick = nick;
        this.uuid = uuid;
    }

    public String getNick() {
        return nick;
    }

    public UUID getUUID() {
        return uuid;
    }

}
