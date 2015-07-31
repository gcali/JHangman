package jhangmanclient.gui;

import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import utility.ReturnCodeObj;
import jhangmanclient.controller.AuthController;
import jhangmanclient.controller.GameController;
import jhangmanclient.controller.LoginResult;
import jhangmanclient.controller.RegistrationResult;

public class AuthPanel extends JPanel implements ActionListener {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    Changer changer = null;
    LabeledField nickComponent;
    LabeledField passwordComponent;
    LogInRegisterButtons buttons;
    private AuthController authController;
    private Consumer<GameController> gameControllerSetter;

    private AuthPanel() {
        super();
    }
    
    public static AuthPanel create(
            AuthController authController, 
            Consumer<GameController> gameControllerSetter) {
        AuthPanel panel = new AuthPanel();
        panel.authController = authController;
        panel.gameControllerSetter = gameControllerSetter;
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
    
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, 
                                      message, 
                                      "Error", 
                                      JOptionPane.ERROR_MESSAGE); 
    }

    private void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(this, 
                                      message, 
                                      "Error", 
                                      JOptionPane.INFORMATION_MESSAGE); 
    }
    
    private int showQuestionDialog(String message, String yes, String no) {
        Object [] options = {yes, no};
        return JOptionPane.showOptionDialog(
                this, 
                message, 
                "", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null, 
                options, 
                options[0]);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
        case "login":
            handleLogin();
            break;
        case "register":
            handleRegister();
        break;
        }
        
    }

    private void handleRegister() {
        String nick;
        String password;
        nick = this.nickComponent.getText();
        password = this.passwordComponent.getText();
        try {
            RegistrationResult retval = 
                    this.authController.handleRegistration(nick, password);
            if (retval == RegistrationResult.ALREADY_REGISTERED) {
                showErrorDialog("User was already registered");
            } else if (retval == RegistrationResult.SUCCESS) {
                showMessageDialog("Registration confirmed");
            }
        } catch (RemoteException e1) {
            showErrorDialog("Couldn't connect to the server");
        } finally {
            this.passwordComponent.clear();
        }
    }

    private void handleLogin() {
        String nick;
        String password;
        nick = this.nickComponent.getText();
        password = this.passwordComponent.getText();
        try {
            ReturnCodeObj<LoginResult, GameController> retval = 
                    this.authController.handleLogin(nick, password, false);
            handleLoginRetval(nick, password, retval);
        } catch (RemoteException e1) {
            showErrorDialog("Couldn't connect to the server");
        } finally {
            this.passwordComponent.clear();
        }
    }

    private void handleLoginRetval(String nick, String password,
            ReturnCodeObj<LoginResult, GameController> retval) {
        switch (retval.getCode()) {
        case ALREADY_LOGGED_IN:
            int answer = showQuestionDialog(
                    "User was already logged in; do you want" +
                    "to force a new login?", "Yes", "No");
            if (answer == 0) {
                handleForceLogin(nick, password);
            }
            break; 
        case WRONG_DATA:
            showErrorDialog("User data invalid");
            break;
        case SUCCESS:
            handleSuccesfullLogin(retval.getObj()); 
        }
    }

    private void handleSuccesfullLogin(GameController gameController) {
        this.nickComponent.clear();
        this.gameControllerSetter.accept(gameController);
        this.changer.changePanel();
    }

    private void handleForceLogin(String nick, String password) {
        try {
            ReturnCodeObj<LoginResult, GameController> retval = 
                    this.authController.handleLogin(nick, password, true);
            handleLoginRetval(nick, password, retval);
        } catch (RemoteException e1) {
            showErrorDialog("Couldn't connect to the server");
        } 
    } 
}