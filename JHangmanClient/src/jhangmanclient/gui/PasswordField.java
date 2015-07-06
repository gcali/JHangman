package jhangmanclient.gui;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class PasswordField extends JPanel {
    
    private JLabel labelComponent;
    private JPasswordField textComponent;

    public PasswordField() {
        super();
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel("Password");
        label.setAlignmentX(LEFT_ALIGNMENT);;
        this.labelComponent = label;
        JPasswordField text = new JPasswordField(20);
        text.setAlignmentX(LEFT_ALIGNMENT);
        text.setMaximumSize(text.getPreferredSize());
        this.textComponent = text;
        this.add(Box.createVerticalGlue());
        this.add(label);
        this.add(text);
        this.setAlignmentX(LEFT_ALIGNMENT);
    }
}
