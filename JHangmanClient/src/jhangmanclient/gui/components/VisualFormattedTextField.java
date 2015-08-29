package jhangmanclient.gui.components;

import java.awt.Color;
import java.text.Format;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import utility.observer.JHObservable;
import utility.observer.JHObservableSupport;
import utility.observer.JHObserver;

/*
 * See http://stackoverflow.com/q/1313390/1076463
 */


public class VisualFormattedTextField extends JFormattedTextField
                                      implements JHObservable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private static final Color WRONG_CONTENT_BACKGROUND = 
            new Color(255,215,215);
    private static final Color WRONG_CONTENT_FOREGROUND = 
            null;
    
    private Color defaultBackground = null;
    private Color defaultForeground = null;
    
    private JHObservableSupport observableSupport = new JHObservableSupport();
    
    private boolean wasValid = false;
    
    public VisualFormattedTextField(Format format) {
        super(new FormatParseAllOrNothing(format));
        this.setFocusLostBehavior(this.PERSIST);
        this.setUpdateVisualsFrequency();
    }
    
    @Override
    public void updateUI() {
        super.updateUI();
        if (this.defaultBackground == null && this.defaultForeground == null) {
            this.defaultBackground = this.getBackground();
            this.defaultForeground = this.getForeground();
        }
    }

    private void setUpdateVisualsFrequency() {
        this.getDocument().addDocumentListener(new DocumentListener() {
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                VisualFormattedTextField.this.updateVisuals();
                
            }
            
            @Override
            public void insertUpdate(DocumentEvent e) {
                VisualFormattedTextField.this.updateVisuals(); 
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                VisualFormattedTextField.this.updateVisuals(); 
            }
        });
    }

    private void updateVisuals() { 
        if (this.isContentValid()) {
            this.setBackground(defaultBackground);
            this.setForeground(defaultForeground);
            if (!this.wasValid) {
                this.observableSupport.publish(new ContentValidityUpdated(true));
                this.setValue(this.getText());
                this.wasValid = true;
            }
        } else {
            if (WRONG_CONTENT_BACKGROUND != null) {
                this.setBackground(WRONG_CONTENT_BACKGROUND);
            }
            if (WRONG_CONTENT_FOREGROUND != null) {
                this.setForeground(WRONG_CONTENT_FOREGROUND);
            }
            if (this.wasValid) {
                this.observableSupport.publish(new ContentValidityUpdated(false));
                this.wasValid = false;
            }
        } 
    }
    
    @Override
    public void setValue(Object value) {
        if (this.isObjectValidInput(value)) {
            super.setValue(value); 
        } else {
            this.updateVisuals();
        }
    }
    
    private boolean isContentValid() {
        return this.isObjectValidInput(this.getText());
    }
    
    private boolean isObjectValidInput(Object input) {
        AbstractFormatter formatter = this.getFormatter();
        if (formatter == null){ 
            return true;
        } else {
            try {
                formatter.valueToString(input);
                return true;
            } catch (ParseException e) {
                return false;
            }
        }
    }

    @Override
    public void addObserver(JHObserver observer) {
        this.observableSupport.add(observer);
    } 
}
