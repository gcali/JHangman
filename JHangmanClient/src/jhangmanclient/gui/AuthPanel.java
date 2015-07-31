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
    
    private void showDialog(String message) {
        JOptionPane.showMessageDialog(this, message); 
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
        case "login":
            String nick = this.nickComponent.getText();
            String password = this.passwordComponent.getText();
            try {
                ReturnCodeObj<LoginResult, GameController> retval = 
                        this.authController.handleLogin(nick, password);
                if (retval.getCode() == LoginResult.ALREADY_LOGGED_IN) {
                   showDialog("User was already logged in");
                } else if (retval.getCode() == LoginResult.WRONG_DATA) {
                    showDialog("User data invalid");
                } else if (retval.getCode() == LoginResult.SUCCESS) {
                    this.gameControllerSetter.accept(retval.getObj());
                    this.changer.changePanel();
                }
            } catch (RemoteException e1) {
                showDialog("Couldn't connect to the server");
            }
            break;
        }
        
    } 
}