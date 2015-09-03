package utility.observer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

//Derived from 
//http://stackoverflow.com/questions/13362636/a-generic-observer-pattern-in-java

/**
 * A support class to help implementation of a {@link JHObservable}. 
 * <p />
 * A {@code JHObservableSupport} offers utilities to {@link #add(JHObserver)}
 * an observer, to {@link #publish(JHEvent)} an event and
 * to {@link #remove(JHObserver)} an observer.
 * 
 * @author gcali
 *
 */
public class JHObservableSupport {
    
    private final Map<Class<? extends JHEvent>, Queue<ObserverInfo>> map = 
            new ConcurrentHashMap<Class<? extends JHEvent>, Queue<ObserverInfo>>();
    
    //use a lock Object to avoid accidental synchronization over this
    //from external use
    private final Object lock = new Object();

    /**
     * Adds a new observer {@code o} to the queue; the observer will be
     * notified of any event which correspond to a {@link ObservationHandler}
     * annotated method. For instance, if the object has the following methods:
     * <ul>
     *      <li>
     *          {@code void onClick(ClickEvent e)}
     *      </li>
     *      <li>
     *          {@code void onMove(MoveEvent e)}
     *      </li>
     * </ul>
     * both annotated with {@link ObservationHandler}, it will be notified when
     * a {@code ClickEvent} or a {@code MoveEvent} are published. Note that
     * e method will be deemed correspondent if and only if its parameter
     * is a super-type of the actual event.
     * 
     * Note: an object is considered an event if and only if it is a sub-type
     * of {@link JHEvent}
     * 
     * @param o the observer to add to the queue
     */
    public void add(JHObserver o) {
        synchronized(lock) {
            for (Method method : o.getClass().getMethods()) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (method.getAnnotation(ObservationHandler.class) != null && 
                    parameterTypes.length == 1) { 
                    //if the current method is correctly annotated and has
                    //only one parameter
    
                    if (JHEvent.class.isAssignableFrom(parameterTypes[0])) {
                        @SuppressWarnings("unchecked")
                        Class<? extends JHEvent> subscribeTo = 
                                (Class<? extends JHEvent>) parameterTypes[0];
                        this.map.putIfAbsent(subscribeTo, 
                                             new ConcurrentLinkedQueue<ObserverInfo>());
                        Queue<ObserverInfo> observerInfos = map.get(subscribeTo);
                        if (observerInfos == null) {
                            //if no other observer expects this kind of event, create
                            //a new queue
                        }
                        //adds the method to its observer queue
                        observerInfos.add(new ObserverInfo(method, o)); 
                    }
                }
            } 
        }
    }

    /**
     * Remove the observer {@code o} from the observer queue
     * @param o the observer to be removed
     */
    public synchronized void remove(JHObserver o) {
        synchronized(lock) {
            for (Queue<ObserverInfo> observerInfos : map.values()) {
                Iterator<ObserverInfo> iterator = observerInfos.iterator();
                while (iterator.hasNext()) {
                    ObserverInfo observer = iterator.next();
                    if (observer.getObserver() == o) {
                        iterator.remove();
                    }
                }
            } 
        }
    }

    public <E extends JHEvent>void publish(E o) {
        List<ObserverInfo> infos = new ArrayList<ObserverInfo>();
        synchronized(lock) {
            for (Map.Entry<Class<? extends JHEvent>,Queue<ObserverInfo>> entry 
                    : map.entrySet()) {
                if (entry.getKey().isAssignableFrom(o.getClass())) {
                    Queue<ObserverInfo> observerInfos = entry.getValue();
                    if (observerInfos != null) {
                        //if the event has been registered at least once
                        for (ObserverInfo observerInfo : observerInfos) {
                            //invoke all the methods registered to that event, if any
//                            observerInfo.invoke(o);
                            infos.add(observerInfo);
                        }
                    } 
                }
            } 
        }
        //out of synch
        for (ObserverInfo obs : infos) {
            obs.invoke(o);
        }
    }

    static class ObserverInfo {
        private final Method method;
        private final JHObserver observer;

        ObserverInfo(Method method, JHObserver observer) {
            this.method = method;
            this.observer = observer;
        }

        synchronized void invoke(Object o) {
            try {
                this.method.setAccessible(true);
                this.method.invoke(this.observer, o);
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }

        synchronized Method getMethod() {
            return this.method;
        }

        synchronized Object getObserver() {
            return this.observer;
        }
    } 
}