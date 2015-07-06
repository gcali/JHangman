package jhangmanclient.gui;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

public class NickField extends JPanel {

    private JLabel labelComponent;
    private JTextField textComponent;

    public NickField() {
        super();
        initComponents();
        handleLayout();
    }

    private void handleLayout() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.add(Box.createVerticalGlue());
        this.add(this.labelComponent);
        this.add(this.textComponent);
        this.setAlignmentX(LEFT_ALIGNMENT);
    }

    private void initComponents() {
        JLabel label = new JLabel("User");
        label.setAlignmentX(LEFT_ALIGNMENT);;
        this.labelComponent = label;
        JTextField text = new JTextField(20);
        text.setAlignmentX(LEFT_ALIGNMENT);
        text.setMaximumSize(text.getPreferredSize());
        this.textComponent = text;
    }
    
    public String getText() {
        return this.textComponent.getText();
    }
}
