package jhangmanclient.gui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import utility.observer.JHObservable;
import utility.observer.JHObservableSupport;
import utility.observer.JHObserver;

/**
 * Published events:
 * <ul>
 *  <li>{@link EmptyFieldEvent}</li>
 *  <li>{@link NonEmptyFieldEvent}</li>
 * </ul>
 * @author gcali
 *
 */
public class WordInputPanel extends JPanel
                            implements JHObservable {
    
    private JTextField textField;
    private final JHObservableSupport observableSupport = 
        new JHObservableSupport();

    public WordInputPanel() {
        initLayout();
        initComponents();
    }

    private void initComponents() {
        textField = new JTextField(10);
        textField.setBackground(Color.WHITE);
        textField.setHorizontalAlignment(JTextField.CENTER);
        add(textField);
        textField.getDocument().addDocumentListener(new DocumentListener() {
            private boolean wasEmpty = true;
            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }
            
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }
            
            private void changed() {
                if (textField.getText() == null || 
                    textField.getText().equals("")) {
                    if (wasEmpty == false) {
                        wasEmpty = true;
                        observableSupport.publish(new EmptyFieldEvent()); 
                    }
                } else if (wasEmpty) {
                    wasEmpty = false;
                    observableSupport.publish(new NonEmptyFieldEvent());
                }
            }
        });
    }
    

    private void initLayout() {
        setLayout(new GridBagLayout()); 
        setBorder(BorderFactory.createTitledBorder("Input"));
    }
    
    public String getText() {
        return textField.getText();
    }
    
    public void clear() {
        textField.setText("");
    }
    
    public void setFontSize(int fontSize) {
        Font oldFont = textField.getFont();
        textField.setFont(new Font(
            oldFont.getName(), 
            oldFont.getStyle(), 
            fontSize
        ));
    } 
    
    public void setLength(int length) {
        textField.setColumns(length);
    }

    @Override
    public void addObserver(JHObserver observer) {
        observableSupport.add(observer);
    }

    @Override
    public void removeObserver(JHObserver observer) {
        observableSupport.remove(observer);
    }
    
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(
            super.getMaximumSize().width,
            getPreferredSize().height
        );
    }
    
    public void setEditable(boolean editable) {
        if (!editable) {
            textField.setBackground(Color.GRAY);
        } else {
            textField.setBackground(Color.WHITE);
        }
        textField.setEditable(editable);
    }
    
}
