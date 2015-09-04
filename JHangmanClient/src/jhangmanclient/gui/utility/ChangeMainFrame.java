package jhangmanclient.gui.utility;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;

import utility.observer.JHObservable;
import utility.observer.JHObservableSupport;
import utility.observer.JHObserver;

public class ChangeMainFrame implements JHObservable {

    private Map<String, JFrame> frames = 
            new ConcurrentHashMap<String, JFrame>();
    private JFrame currentlyVisible = null;
    
    private JHObservableSupport observableSupport = new JHObservableSupport();
    
    public ChangeMainFrame() {
    }
    
    
    private void changePanelTo(JFrame frame) { 
        if (currentlyVisible != null) {
            currentlyVisible.setVisible(false);
        }
        frame.setVisible(true);
        frame.toFront();
        frame.requestFocus();
        currentlyVisible = frame;
    }

    public void addPanel(JFrame container, String id) {
        container.setVisible(false);
        this.frames.put(id, container); 
    }
    
    public void changeFrame(String id) {
        JFrame frame = this.frames.get(id);
        if (frame != null) {
            changePanelTo(frame);
        }
    }
    
    public void publishNickChange(String nick) {
        System.out.println("Published nick event");
        this.observableSupport.publish(new SetNickEvent(nick));
    }


    @Override
    public void addObserver(JHObserver observer) {
        this.observableSupport.add(observer); 
    }


    @Override
    public void removeObserver(JHObserver observer) {
        this.observableSupport.remove(observer);
    }
}