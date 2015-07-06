package jhangmanclient.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class LoginRegisterButtons extends Box {
    
    private JButton loginButton;
    private JButton registerButton;
    private UserDataAction loginAction = null;
    private UserDataAction registerAction;

    public LoginRegisterButtons() {
        super(BoxLayout.LINE_AXIS);
        initializeComponents();
        handleLayout();
    }


    private void handleLayout() {
        this.add(Box.createHorizontalStrut(20));
        this.add(this.loginButton);
        this.add(createHorizontalStrut(10));
        this.add(this.registerButton);
        this.add(Box.createHorizontalGlue());
        this.setAlignmentX(LEFT_ALIGNMENT);
    }


    private void initializeComponents() {
        JButton login;
        login = new JButton("Login");
        login.setActionCommand("login");
        this.loginButton = login;
        JButton register;
        register = new JButton("Register");
        register.setActionCommand("register");
        this.registerButton = register;
    } 
    
    public void addActionListener(ActionListener l) {
        this.loginButton.addActionListener(l);
        this.registerButton.addActionListener(l);
    } 
}