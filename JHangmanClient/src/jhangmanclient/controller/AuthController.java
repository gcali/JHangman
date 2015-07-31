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
    
    public ReturnCodeObj<LoginResult, GameController> handleLogin(
            String nick, 
            String password) throws RemoteException {

        try {
            int cookie = this.server.logIn(nick, password, null);
            GameController gameController = new GameController(this.server, 
                                                               nick, 
                                                               cookie);
            return new ReturnCodeObj<LoginResult, GameController>(
                    LoginResult.SUCCESS, 
                    gameController);
        } catch (RemoteException e) {
            throw e;
        } catch (UserAlreadyLoggedInException e) {
            return new ReturnCodeObj<LoginResult, GameController>(
                    LoginResult.ALREADY_LOGGED_IN, 
                    null);
        } catch (WrongPasswordException e) {
            return new ReturnCodeObj<LoginResult, GameController>(
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