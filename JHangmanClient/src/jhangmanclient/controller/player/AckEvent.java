package jhangmanclient.controller.player;

import java.util.UUID;

import utility.observer.JHEvent;

public class AckEvent implements JHEvent {
    
    UUID uuid;

    AckEvent(UUID uuid) {
        this.uuid = uuid;
    }
    
    UUID getUUID() {
        return uuid;
    }

}
