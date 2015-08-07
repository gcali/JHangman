package jhangmanclient.controller;

import java.rmi.RemoteException;

import rmi_interface.RMIServer;
import rmi_interface.UserAlreadyLoggedInException;
import rmi_interface.UserAlreadyRegisteredException;
import rmi_interface.WrongPasswordException;
import utility.ReturnCodeObj;

public class AuthController {

    private RMIServer server;

    public AuthController(RMIServer server) {
        this.server = server;
    }
    
    public ReturnCodeObj<LoginResult, GameChooserController> handleLogin(
            String nick, 
            String password,
            boolean forced) throws RemoteException {

        try {
            int cookie;
            if (!forced) {
                cookie = this.server.logIn(nick, password, null);
            } else {
                cookie = this.server.forceLogIn(nick, password, null);
            }
            GameChooserController gameChooserController = new GameChooserController(this.server, 
                                                               nick, 
                                                               cookie);
            return new ReturnCodeObj<LoginResult, GameChooserController>(
                    LoginResult.SUCCESS, 
                    gameChooserController);
        } catch (RemoteException e) {
            throw e;
        } catch (UserAlreadyLoggedInException e) {
            return new ReturnCodeObj<LoginResult, GameChooserController>(
                    LoginResult.ALREADY_LOGGED_IN, 
                    null);
        } catch (WrongPasswordException e) {
            return new ReturnCodeObj<LoginResult, GameChooserController>(
                    LoginResult.WRONG_DATA, 
                    null);
        } 
    }
    
    public RegistrationResult handleRegistration(String user, String password) 
            throws RemoteException {
        try {
            this.server.register(user, password);
            return RegistrationResult.SUCCESS;
        } catch (RemoteException e) {
            throw e;
        } catch (UserAlreadyRegisteredException e) {
            return RegistrationResult.ALREADY_REGISTERED;
        }
    }

}