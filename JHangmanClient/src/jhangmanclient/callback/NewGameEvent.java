package jhangmanclient.callback;

import utility.observer.JHEvent;

public class NewGameEvent implements JHEvent {
    
    private String name;

    public NewGameEvent(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }

}
