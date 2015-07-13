package jhangmanclient.gui;

import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;

public class SigninRegisterButtons extends Box {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JButton signinButton;
    private JButton registerButton;

    public SigninRegisterButtons() {
        super(BoxLayout.LINE_AXIS);
        initializeComponents();
        handleLayout();
    }


    private void handleLayout() {
        this.add(Box.createHorizontalStrut(20));
        this.add(this.signinButton);
        this.add(createHorizontalStrut(10));
        this.add(this.registerButton);
        this.add(Box.createHorizontalGlue());
        this.setAlignmentX(LEFT_ALIGNMENT);
    }


    private void initializeComponents() {
        JButton login;
        login = new JButton("Sign in");
        login.setActionCommand("signin");
        this.signinButton = login;
        JButton register;
        register = new JButton("Register");
        register.setActionCommand("register");
        this.registerButton = register;
    } 
    
    public void addActionListener(ActionListener l) {
        this.signinButton.addActionListener(l);
        this.registerButton.addActionListener(l);
    } 
}