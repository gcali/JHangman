package jhangmanclient.gui.components;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jhangmanclient.gui.frames.HangmanFrame;
import utility.observer.JHObserver;
import utility.observer.ObservationHandler;

public class AskPositiveNumberDialog extends JDialog {

    private VisualFormattedTextField textField;
    private JOptionPane optionPane;
    private JButton okButton;
    private Long value;

    public AskPositiveNumberDialog(JFrame frame, String question) {
        super(frame, "Enter a number");
        
        VisualFormattedTextField textField = 
                new VisualFormattedTextField(NumberFormat.getIntegerInstance());
        textField.setColumns(2);
        textField.validate();
        JPanel textPanel = new JPanel();
        LayoutManager layout = new BoxLayout(textPanel, BoxLayout.LINE_AXIS);
        textPanel.add(textField);
        textPanel.setMaximumSize(new Dimension(200,500));
        this.textField = textField;
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("Ok");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        JLabel questionLabel = new JLabel(question + " ");
        Font font = questionLabel.getFont();
        questionLabel.setFont(new Font(font.getName(), 
                                       Font.BOLD | Font.ITALIC, 
                                       15));
        this.okButton = okButton;
        JPanel totalPanel = new JPanel();
        layout = new BoxLayout(totalPanel, BoxLayout.PAGE_AXIS);
        totalPanel.setLayout(layout);
        questionLabel.setAlignmentX(CENTER_ALIGNMENT);
        textPanel.setAlignmentX(CENTER_ALIGNMENT);
        buttonPanel.setAlignmentX(CENTER_ALIGNMENT);
        totalPanel.add(questionLabel);
        totalPanel.add(Box.createVerticalStrut(10));
        totalPanel.add(textPanel);
        totalPanel.add(buttonPanel);
        totalPanel.setBorder(HangmanFrame.createEmptyBorder(10));
        this.setContentPane(totalPanel);
        
        this.textField.addObserver(new JHObserver() { 
            @ObservationHandler
            public void onContentEvent(ContentValidityUpdated e) { 
                okButton.setEnabled(e.isValid());
            }
        });
        
        okButton.addActionListener(new ActionListener() { 
            @Override
            public void actionPerformed(ActionEvent e) {
                AskPositiveNumberDialog.this.handleOk();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                AskPositiveNumberDialog.this.handleCancel();
                
            }
        });
        
        this.setModalityType(ModalityType.APPLICATION_MODAL);
        this.setVisible(false);
        this.pack(); 
        this.setLocationRelativeTo(null);
    }

    private void handleCancel() {
        this.setValue(null);
        this.hideAndClear();
    }

    private void setValue(Long value) {
        this.value = value;
    }

    private void handleOk() {
        Object value = this.textField.getValue();
        if (value instanceof Number) {
            this.value = ((Number)value).longValue();
        } else {
            this.value = null;
        }
        this.hideAndClear();
    }
    
    private void hideAndClear() {
        this.okButton.setEnabled(false);
        this.textField.reset();
        this.setVisible(false);
    }

    public Long getPlayers() {
        this.setVisible(true);
        System.out.println(this.getValue());
        return this.getValue();
    }

    private Long getValue() {
        return this.value;
    }

}
