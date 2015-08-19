package utility.observer;

public interface JHObservable {
    
    /**
     * Add a new observer to be notified when events are published. 
     * @param observer the observer to add to the notification queue
     */
    public void addObserver(JHObserver observer);

}
