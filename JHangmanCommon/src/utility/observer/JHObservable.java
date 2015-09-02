package utility.observer;

public interface JHObservable {
    
    /**
     * Add a new observer to be notified when events are published. 
     * @param observer the observer to add to the notification queue
     */
    public void addObserver(JHObserver observer);
    
    /**
     * Remove an observer from the queue; the observer will not be notified
     * of any new event from this {@link JHObservable} after a successful
     * removal
     * @param observer the observer to be removed from the notification
     *                 queue
     */
    public void removeObserver(JHObserver observer);

}
