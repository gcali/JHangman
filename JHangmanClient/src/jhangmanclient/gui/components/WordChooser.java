package jhangmanclient.gui.components;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import utility.observer.JHObservable;
import utility.observer.JHObservableSupport;
import utility.observer.JHObserver;

/**
 * @author gcali
 * 
 * Published events:
 *      WordSubmittedEvent
 *
 */
public class WordChooser extends JPanel
                         implements JHObservable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int columns;
    private JTextField textField;
    private JButton submitButton;
    
    private JHObservableSupport observableSupport = new JHObservableSupport();
    private String word;

    public WordChooser() {
        this(10);
    } 
    
    public WordChooser(int columns) {
        super();
        this.columns = columns;
        this.initLayout();
        this.initComponents();
        this.placeComponents();
    }
    
    private void initLayout() {
        this.setLayout(new GridBagLayout());
    }
    
    private void initComponents() {
        this.textField = new JTextField(this.columns);
        this.submitButton = new JButton("Submit");
        this.submitButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                String word = textField.getText();
                setWord(word);
                submitButton.setEnabled(false);
                textField.setEnabled(false); 
            }
        });
    }
    
    private void setWord(String word) {
        this.word = word; 
        this.observableSupport.publish(new WordSubmittedEvent(this.word));
    }
    
    public String getWord() {
        return this.word;
    }

    private void placeComponents() {
        GridBagConstraints c = null;
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2; 
        c.weightx = 0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.LINE_START;
        this.add(this.textField, c);
        c.gridx = 1;
        this.add(this.submitButton, c); 
        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridheight = 3;
        this.add(new JPanel(), c);
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        EventQueue.invokeLater(new Runnable() { 
            @Override
            public void run() {
                frame.add(new WordChooser(10));
                frame.pack();
                frame.setVisible(true); 
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            }
        });
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