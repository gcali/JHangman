package jhangmanclient.gui.frames;

import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import jhangmanclient.gui.components.ContentValidityUpdated;
import jhangmanclient.gui.components.VisualFormattedTextField;
import utility.observer.JHObserver;
import utility.observer.ObservationHandler;


public class TestVisualFormatted extends HangmanFrame implements JHObserver {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JButton button;

    @Override
    protected void initComponents() {
        JPanel panel = new JPanel();
        LayoutManager layout = new BoxLayout(panel, BoxLayout.LINE_AXIS);
        panel.setLayout(layout);
        
        VisualFormattedTextField textField = new VisualFormattedTextField(NumberFormat.getIntegerInstance());
        JButton button = new JButton("Add text");
        JTextArea textArea = new JTextArea(20, 20);
        panel.add(textField);
        panel.add(button);
        
        
        this.button = button;
        button.setEnabled(false);

        textField.addObserver(this);
        
        button.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                Object value = textField.getValue();
                if (value != null) {
                    textArea.append(NumberFormat.getIntegerInstance().format(value));
                    textArea.append("\n");
                }
            }
        });
        this.add(panel);
        this.add(textArea);
    } 
    
    @ObservationHandler
    public void onContentValidityUpdatedEvent(ContentValidityUpdated e) {
        if (e.isValid()) {
            this.button.setEnabled(true);
        } else {
            this.button.setEnabled(false);
        }
    }
    
    public static void main(String[] args) throws ParseException {
        TestVisualFormatted frame = new TestVisualFormatted();
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                frame.setVisible(true); 
            }
        });
//        NumberFormat format = NumberFormat.getIntegerInstance();
//        
//        System.out.println(format.parseObject("1"));
    }
}