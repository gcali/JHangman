package jhangmanclient.game_data;

import utility.observer.JHEvent;

public class RemovedGameEvent implements GameDataChangedEvent { 
    
    private String name;

    public RemovedGameEvent(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }

}
