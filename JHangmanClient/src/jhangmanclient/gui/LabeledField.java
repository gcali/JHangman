package jhangmanclient.gui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LabeledField extends Box {
    
    private JLabel labelComponent;
    private JTextField textComponent;

    public LabeledField(String label, boolean hiddenText) {
        super(BoxLayout.PAGE_AXIS);
        handleLayout(label, hiddenText);
    }

    private void handleLayout(String label, boolean hiddenText) {
        initComponents(label, hiddenText);
        this.add(Box.createVerticalGlue());
        this.add(this.labelComponent);
        this.add(this.textComponent);
        this.setAlignmentX(LEFT_ALIGNMENT);
    }

    private void initComponents(String label, boolean hiddenText) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setAlignmentX(LEFT_ALIGNMENT);;
        this.labelComponent = labelComponent;
        JTextField textComponent = null;
        int minimumWidth = 20;
        if (hiddenText) {
            textComponent = new JPasswordField(minimumWidth);
        } else {
            textComponent = new JTextField(minimumWidth);
        }
        textComponent.setAlignmentX(LEFT_ALIGNMENT);
        textComponent.setBackground(new Color(255,255,255));
        int fixedHeight = (int) textComponent.getPreferredSize().getHeight();
        textComponent.setMaximumSize(new Dimension(Integer.MAX_VALUE, fixedHeight));
        textComponent.setMinimumSize(new Dimension(minimumWidth, fixedHeight));
        this.textComponent = textComponent;
    } 
    
    public String getText() {
        return this.textComponent.getText();
    }
}