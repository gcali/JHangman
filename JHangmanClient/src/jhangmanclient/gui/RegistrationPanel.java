package jhangmanclient.gui;

import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

public class RegistrationPanel extends JPanel implements ActionListener {
    
    Changer changer = null;
    LabeledField nickComponent;
    LabeledField passwordComponent;
    LogInRegisterButtons buttons;

    private RegistrationPanel() {
        super();
    }
    
    public static RegistrationPanel create() {
        RegistrationPanel panel = new RegistrationPanel();
        panel.initLayout();
        panel.initComponents();
        return panel;
    }
    
    private void initComponents() { 
        this.nickComponent = new LabeledField("User", false);
        this.passwordComponent = new LabeledField("Password", true);
        this.buttons = new LogInRegisterButtons();
        this.buttons.addActionListener(this);
        this.add(this.nickComponent);
        this.add(this.passwordComponent);
        this.add(this.buttons); 
    }

    private void initLayout() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS)); 
    }

    public void setChanger(Changer changer) {
        this.changer = changer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
        case "login":
            System.out.println("Login!");
            System.out.printf("User: %s, Password: %s\n", this.nickComponent.getText(),
                                                          this.passwordComponent.getText());
            break;
        }
        
    } 
}