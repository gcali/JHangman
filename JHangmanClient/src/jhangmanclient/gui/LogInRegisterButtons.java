package jhangmanclient.gui;

import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

public class LogInRegisterButtons extends Box {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JButton logInButton;
    private JButton registerButton;

    public LogInRegisterButtons() {
        super(BoxLayout.LINE_AXIS);
        initializeComponents();
        handleLayout();
    }


    private void handleLayout() {
        this.add(Box.createHorizontalStrut(20));
        this.add(this.logInButton);
        this.add(createHorizontalStrut(10));
        this.add(this.registerButton);
        this.add(Box.createHorizontalGlue());
        this.setAlignmentX(LEFT_ALIGNMENT);
    }


    private void initializeComponents() {
        JButton logIn;
        logIn = new JButton("Log in");
        logIn.requestFocusInWindow();
        logIn.setActionCommand("login");
        this.logInButton = logIn;
        JButton register;
        register = new JButton("Register");
        register.setActionCommand("register");
        this.registerButton = register;
    } 
    
    public void addActionListener(ActionListener l) {
        this.logInButton.addActionListener(l);
        this.registerButton.addActionListener(l);
    }

}