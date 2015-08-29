package jhangmanclient.gui.components;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class AskPositiveNumberDialog extends JDialog {

    private VisualFormattedTextField textField;
    private JOptionPane optionPane;

    public AskPositiveNumberDialog(JFrame frame, String question) {
        super(frame, "Enter a number");
//        this.frame = frame;
//        this.question = question;
        
        VisualFormattedTextField textField = 
                new VisualFormattedTextField(NumberFormat.getIntegerInstance());
        textField.setColumns(20);
        this.textField = textField;
        Object[] optionsArray = {question, textField};
        JOptionPane optionPane = new JOptionPane(
                optionsArray, 
                JOptionPane.QUESTION_MESSAGE, 
                JOptionPane.OK_CANCEL_OPTION, 
                null); 
        textField.addPropertyChangeListener("value", 
                                            new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                optionPane.setValue(evt.getNewValue());
                
            }
        });
        this.optionPane = optionPane;
        this.setModalityType(ModalityType.APPLICATION_MODAL);
        this.setVisible(false);
        this.setContentPane(optionPane);
        this.pack();
        
    }

    public Integer getPlayers() {
        this.textField.setText("");
        this.setVisible(true);
        System.out.println(this.optionPane.getValue());
        this.setVisible(false);
        this.textField.setText("");
        return 12;
    }

}
