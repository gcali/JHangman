package jhangmanclient.gui.components;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class NotificationPanel extends JPanel {
    
    JScrollPane scrollPane;
    JTextArea outputTextArea;
    private int columns;
    
    private final Object lock = new Object();
    
    public NotificationPanel() {
        this(20);
    }
    
    public NotificationPanel(int columns) { 
        super();
        this.columns = columns;
        initLayout();
        initComponents();
    }

    private void initComponents() {
        outputTextArea = new JTextArea(0, columns);
        scrollPane = new JScrollPane(outputTextArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        outputTextArea.setEditable(false);
        outputTextArea.setWrapStyleWord(true);
        outputTextArea.setLineWrap(true);
        scrollPane.setBorder(BorderFactory.createLoweredBevelBorder()); 
        
        this.add(scrollPane);
    }

    private void initLayout() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        
    }
    
    public void addLine(String text) {
        synchronized(lock) {
            outputTextArea.append(text);
            outputTextArea.append("\n"); 
        }
    }
    
    public void setText(String text) {
        synchronized(lock) {
            outputTextArea.setText(text); 
        }
    }
    
    public void clear() {
        synchronized(lock) {
            outputTextArea.setText(""); 
        }
    }

}
