package jhangmanclient.gui.components;

import utility.observer.JHEvent;

public class ContentValidityUpdated implements JHEvent {
    
    private boolean valid;

    public ContentValidityUpdated(boolean valid) {
        this.valid = valid;
    }
    
    public boolean isValid() {
        return this.valid;
    }

}
