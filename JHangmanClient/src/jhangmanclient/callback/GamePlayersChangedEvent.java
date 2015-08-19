package jhangmanclient.callback;

import utility.observer.JHEvent;

public class GamePlayersChangedEvent implements JHEvent {
    
    private String name;

    public GamePlayersChangedEvent(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }

}
