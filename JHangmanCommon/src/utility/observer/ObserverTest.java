package utility.observer;

public class ObserverTest {
    
    static class Observed implements JHObservable {
        
        private JHObservableSupport support = new JHObservableSupport();

        @Override
        public void addObserver(JHObserver observer) {
            this.support.add(observer); 
        } 
        
        public <E extends JHEvent> void fireEvent(E event) {
            this.support.publish(event);
        }

        @Override
        public void removeObserver(JHObserver observer) {
            this.support.remove(observer);
            
        }
    }
    
    static class EventA implements JHEvent {
        
    }
    
    static class EventB implements JHEvent {
        
    }
    
    static class EventC extends EventA {
        
    }
    
    static class Observer implements JHObserver {

        @ObservationHandler
        public void onEventA(EventA event) {
            System.out.println("Got the event A"); 
            System.out.println(event.getClass());
        }
        
        @ObservationHandler
        public void onEventB(EventB event) {
            System.out.println("Got the event B"); 
            System.out.println(event.getClass());
        }
        
        @ObservationHandler
        public void onEventAncestor(JHEvent event) {
            System.out.println("Got the event ancestor"); 
            System.out.println(event.getClass()); 
        }
        
    }

    public static void main(String[] args) {
        
        Observed observed = new Observed();
        Observer observer = new Observer();
        
        
        observed.addObserver(observer);
        System.out.println("Observer added");
        observed.fireEvent(new EventA());
        observed.fireEvent(new EventB());
        observed.fireEvent(new EventC());
        observed.fireEvent(new EventA());
        observed.fireEvent(new JHEvent() {});
        observed.removeObserver(observer);
        System.out.println("After removal...");
        observed.fireEvent(new EventA());
        System.out.println("Test done");
        
    }

}
