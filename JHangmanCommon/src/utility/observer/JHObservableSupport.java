package utility.observer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    
    private final Map<Class<? extends JHEvent>, List<ObserverInfo>> map = 
            new LinkedHashMap<Class<? extends JHEvent>, List<ObserverInfo>>();

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
                    List<ObserverInfo> observerInfos = map.get(subscribeTo);
                    if (observerInfos == null) {
                        //if no other observer expects this kind of event, create
                        //a new queue
                        map.put(subscribeTo, observerInfos = 
                            new ArrayList<ObserverInfo>());
                    }
                    //adds the method to its observer queue
                    observerInfos.add(new ObserverInfo(method, o)); 
                }
            }
        }
    }

    /**
     * Remove the observer {@code o} from the observer queue
     * @param o the observer to be removed
     */
    public void remove(JHObserver o) {
        for (List<ObserverInfo> observerInfos : map.values()) {
            Iterator<ObserverInfo> iterator = observerInfos.iterator();
            while (iterator.hasNext()) {
                ObserverInfo observer = iterator.next();
                if (observer.getObserver() == o) {
                    iterator.remove();
                }
            }
        }
    }

    public <E extends JHEvent>void publish(E o) {
        for (Map.Entry<Class<? extends JHEvent>,List<ObserverInfo>> entry 
                : map.entrySet()) {
            if (entry.getKey().isAssignableFrom(o.getClass())) {
                List<ObserverInfo> observerInfos = entry.getValue();
                if (observerInfos != null) {
                    //if the event has been registered at least once
                    for (ObserverInfo observerInfo : observerInfos) {
                        //invoke all the methods registered to that event, if any
                        observerInfo.invoke(o);
                    }
                } 
            }
        }
    }

    static class ObserverInfo {
        private final Method method;
        private final JHObserver observer;

        ObserverInfo(Method method, JHObserver observer) {
            this.method = method;
            this.observer = observer;
        }

        void invoke(Object o) {
            try {
                this.method.invoke(this.observer, o);
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }

        public Method getMethod() {
            return this.method;
        }

        public Object getObserver() {
            return this.observer;
        }
    } 
}