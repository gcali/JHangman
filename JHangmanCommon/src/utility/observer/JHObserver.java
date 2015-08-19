package utility.observer;

/**
 * Represents an observer.
 * 
 * Every JHObserver can be used to observe events published by a
 * {@link JHObservable}. An event is categorized by its event parameter;
 * every event parameter must be a subclass of {@link JHEvent}. When an event
 * of type {@code <E extends JHEvent>} is published, any method from the observer
 * with signature {@code void onEvent(T event)} (with {@code T} subclass of
 *  {@code E}) and annotated with {@link ObservationHandler} is called.
 * 
 * @author gcali
 *
 */
public interface JHObserver {

}
