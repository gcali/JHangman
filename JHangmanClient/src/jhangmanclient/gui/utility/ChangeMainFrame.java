package jhangmanclient.gui.utility;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;

public class ChangeMainFrame implements Changer {
    private Map<String, JFrame> frames = 
            new ConcurrentHashMap<String, JFrame>();
    private JFrame currentlyVisible = null;
    
    public ChangeMainFrame() {
    }
    
    
    private void changePanelTo(JFrame frame) { 
        if (currentlyVisible != null) {
            currentlyVisible.setVisible(false);
        }
        frame.setVisible(true);
        currentlyVisible = frame;
    }

    public void addPanel(JFrame container, String id) {
        container.setVisible(false);
        this.frames.put(id, container); 
    }
    
    @Override
    public void changeFrame(String id) {
        JFrame frame = this.frames.get(id);
        if (frame != null) {
            changePanelTo(frame);
        }
    }
}