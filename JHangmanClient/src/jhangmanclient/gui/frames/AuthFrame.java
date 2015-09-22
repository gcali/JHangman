package jhangmanclient.gui.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.function.Consumer;

import javax.swing.JFrame;

import jhangmanclient.controller.common.AuthController;
import jhangmanclient.controller.common.GameChooserController;
import jhangmanclient.controller.common.LoginResult;
import jhangmanclient.controller.common.RegistrationResult;
import jhangmanclient.gui.components.LabeledField;
import jhangmanclient.gui.components.LogInRegisterButtons;
import jhangmanclient.gui.utility.ChangeMainFrame;
import utility.ReturnCodeObj;

/**
 * 
 * A {@link JFrame} to be used during the authentication phase of the
 * application; it handles registration and login.
 * <p/>
 * An {@code AuthFrame} correctly initialized yields its control after
 * a successful login, by changing to the frame identified by
 * {@link GameChooserFrame#idString}
 * 
 * @author gcali
 *
 */
public class AuthFrame extends HangmanFrame implements ActionListener {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Window changer; used to change to other windows when needed
     */
    private ChangeMainFrame changer = null;
    /**
     * Nick component of the login labels
     */
    private LabeledField nickComponent;
    /**
     * Password component of the login labels
     */
    private LabeledField passwordComponent;
    /**
     * Log in/register buttons
     */
    private LogInRegisterButtons buttons;
    /**
     * Controller to interface with the underlying authentication data
     * and decouple GUI/logic
     */
    private AuthController authController;
    /**
     * Procedure to be called to set the {@link GameChooserController} to the
     * correct structure; basically, when a new gameController
     * is created, it must be set using this procedure to be sure the
     * correct frame can use it.
     */
    private Consumer<GameChooserController> gameControllerSetter;

    /**
     * Create a new {@link AuthFrame}; its components are initialized 
     * and positioned and its visibility is set to false
     * @param authController        the controller to interface with the actual
     *                              data for authentication
     * @param gameControllerSetter  the procedure to be called on a new
     *                              {@link GameChooserController} creation
     *                              to link the controller with the right
     *                              structures
     * @return  The newly created {@link AuthFrame}
     */
    public AuthFrame (
            AuthController authController, 
            Consumer<GameChooserController> gameControllerSetter,
            ChangeMainFrame changer) {
        super(10);
        this.authController = authController;
        this.gameControllerSetter = gameControllerSetter;
        this.changer = changer;
        this.setVisible(false);
    }
    
    /**
     * Handle the initializations of the different internal components
     * (buttons, labels and so on)
     */
    @Override
    protected void initComponents() { 
        this.nickComponent = new LabeledField("User", false);
        this.passwordComponent = new LabeledField("Password", true);
        this.buttons = new LogInRegisterButtons();
        this.buttons.addActionListener(this);
        this.add(this.nickComponent);
        this.add(this.passwordComponent);
        this.add(this.buttons); 
    }

    /**
     * Sets the {@link ChangeMainFrame} for this structure;
     *  the {@link ChangeMainFrame} is used
     * to change the currently visible frame, when needed.
     * @param changer   the {@link ChangeMainFrame} to be used
     */
    public void setChanger(ChangeMainFrame changer) {
        this.changer = changer;
    }
    
    /**
     * {@inheritDoc}
     * 
     * Initiate handling of login and registration events; 
     * shouldn't be overridden
     */
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

    /**
     * Handles a registration event
     */
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

    /**
     * Handles a login event
     */
    private void handleLogin() {
        String nick;
        String password;
        nick = this.nickComponent.getText();
        password = this.passwordComponent.getText();
        try {
            ReturnCodeObj<LoginResult, GameChooserController> retval = 
                    this.authController.handleLogin(nick, password, false);
            handleLoginRetval(nick, password, retval);
        } catch (RemoteException e1) {
            showErrorDialog("Couldn't connect to the server");
            e1.printStackTrace();
        } finally {
            this.passwordComponent.clear();
        }
    }
    
    public void setNick(String nick) {
        this.nickComponent.setText(nick);
    }
    
    public void setPass(String pass) {
        this.passwordComponent.setText(pass);
    }

    /**
     * Handles the result of a login
     * @param nick      the nick of the user trying to log in
     * @param password  the password of the user trying to log in
     * @param retval    the result of the last log in attempt
     */
    private void handleLoginRetval(String nick, String password,
            ReturnCodeObj<LoginResult, GameChooserController> retval) {
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
            handleSuccesfulLogin(nick, retval.getObj()); 
        }
    }

    /**
     * Handles the results of a successful login
     * @param gameChooserController the controller to set for the next gaming
     *                              phase
     */
    private void handleSuccesfulLogin(
            String nick,
            GameChooserController gameChooserController
    ) {
        this.nickComponent.clear();
        this.gameControllerSetter.accept(gameChooserController);
        this.changer.publishNickChange(nick);
        this.changer.changeFrame(GameChooserFrame.idString);
    }

    /**
     * Handles a login where a user might be already logged in, by kicking him
     * out
     * @param nick      the nick of the user trying to log in
     * @param password  the password of the user trying to log in
     */
    private void handleForceLogin(String nick, String password) {
        try {
            ReturnCodeObj<LoginResult, GameChooserController> retval = 
                    this.authController.handleLogin(nick, password, true);
            handleLoginRetval(nick, password, retval);
        } catch (RemoteException e1) {
            showErrorDialog("Couldn't connect to the server");
        } 
    } 
}