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
    
    private void setValidVisual() {
        this.setBackground(defaultBackground);
        this.setForeground(defaultForeground); 
    }
    
    private void setWrongVisual() {
        if (WRONG_CONTENT_BACKGROUND != null) {
            this.setBackground(WRONG_CONTENT_BACKGROUND);
        }
        if (WRONG_CONTENT_FOREGROUND != null) {
            this.setForeground(WRONG_CONTENT_FOREGROUND);
        } 
    }

    private void updateVisuals() { 
        if (this.isContentValid()) {
            this.setValidVisual();
            try {
                this.commitEdit();
            } catch (ParseException e) {
            }
            if (!this.wasValid) {
                this.observableSupport.publish(new ContentValidityUpdated(true));
                this.wasValid = true;
            }
        } else {
            this.setWrongVisual();
            if (this.wasValid) {
                this.observableSupport.publish(new ContentValidityUpdated(false));
                this.wasValid = false;
            }
        } 
    }
    
    private Object getValueFromString(String text) {
        AbstractFormatter formatter = this.getFormatter();
        try {
            return formatter.stringToValue(text);
        } catch (ParseException e) {
            return null;
        }
    }
    
    @Override
    public void setValue(Object value) {
        boolean valid = false;

        AbstractFormatter formatter = this.getFormatter();
        try {
            valid = this.isStringValidInput(formatter.valueToString(value));
        } catch (ParseException e ) {
            
        }
        if (valid) {
            super.setValue(value); 
        } else {
            this.updateVisuals();
        } 
    }
    
    private boolean isContentValid() {
        return this.isStringValidInput(this.getText());
    }
    
    private boolean isStringValidInput(String input) {
        AbstractFormatter formatter = this.getFormatter();
        if (formatter == null){ 
            return true;
        } else {
            try {
                formatter.stringToValue(input);
                return true;
            } catch (ParseException e) {
                return false;
            }
        }
    }
    
    @Override
    public Object getValue() {
        if (this.isEditValid()) {
            return super.getValue(); 
        } else {
            return null;
        }
    }

    @Override
    public void addObserver(JHObserver observer) {
        this.observableSupport.add(observer);
    }

    public void reset() {
        this.setText(""); 
        this.wasValid = false;
        this.setValidVisual();
    } 
}
