package jhangmanclient.callback;

import utility.observer.JHEvent;

public class RemovedGameEvent implements JHEvent { 
    
    private String name;

    public RemovedGameEvent(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }

}
