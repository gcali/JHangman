package utility;


/**
 * Azione da aggiungere ad un {@link ActionExecutor}
 * @author gcali
 *
 */
public class Action {
    
    Runnable action;
    int id;
    
    public Action(Runnable action, int id) {
        this.action = action;
        this.id = id;
    }
    
    public void execute() {
        this.action.run();
    }

    public int getId() {
        return id;
    } 
}
