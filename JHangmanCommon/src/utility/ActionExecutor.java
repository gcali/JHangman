package utility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ActionExecutor {
    
    private List<Action> actions;
    private int id;
    
    private static final ActionExecutor singletonExecutor = 
            createActionExecutor();

    private ActionExecutor() {
        this.actions = new ArrayList<Action>();
        this.id = 0;
    }
    
    private static ActionExecutor createActionExecutor() {
        ActionExecutor executor = new ActionExecutor();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                executor.executeAllActions();
            }
        });
        return executor;
    }
    
    public static ActionExecutor getActionExecutor() {
        return singletonExecutor;
    }
    
    public synchronized int addAction(Runnable action) {
        this.actions.add(new Action(action, id));
        return this.id++;
    } 
    
    public synchronized void executeAction(int id) {
        Action action = getAction(id);
        if (action != null) {
            action.execute();
        }
    }
    
    public synchronized void executeAllActions() {
        for (Action action : this.actions) {
            action.execute();
        }
        this.actions = new ArrayList<Action>();
    }

    public void removeAction(int id) {
        getAction(id); 
    }
    
    private synchronized Action getAction(int id) {
        Iterator<Action> iter = this.actions.iterator();
        while (iter.hasNext()) {
            Action action = iter.next();
            if (action.getId() == id) {
                iter.remove();
                return action;
            }
        }
        return null;
    }
}