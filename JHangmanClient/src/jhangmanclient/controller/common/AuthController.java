package jhangmanclient.controller.common;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import jhangmanclient.callback.GameListCallback;
import jhangmanclient.config.ClientConfigData;
import rmi_interface.ClientCallbackRMI;
import rmi_interface.RMIServer;
import rmi_interface.UserAlreadyLoggedInException;
import rmi_interface.UserAlreadyRegisteredException;
import rmi_interface.WrongPasswordException;
import utility.ReturnCodeObj;

public class AuthController {

    private RMIServer server;
    private GameListCallback callback;
    private ClientConfigData configData;
    
    public AuthController(
        RMIServer server,
        ClientConfigData configData
    ) throws RemoteException {
        this.configData = configData;
        this.server = server;
        this.callback = null;
    }
    
    
    public ReturnCodeObj<LoginResult, GameChooserController> handleLogin(
            String nick, 
            String password,
            boolean forced) throws RemoteException {

        try {
            int cookie;
            ClientCallbackRMI exportedCallback = this.renewCallback();
            if (!forced) {
                cookie = this.server.logIn(nick, 
                                           password, 
                                           exportedCallback);
            } else {
                cookie = this.server.forceLogIn(nick, 
                                                password, 
                                                exportedCallback);
            }
            GameChooserController gameChooserController = 
                    new GameChooserController(this.server, 
                                              nick, 
                                              cookie, 
                                              this.callback,
                                              configData);

            return new ReturnCodeObj<LoginResult, GameChooserController>(
                    LoginResult.SUCCESS, 
                    gameChooserController
            );

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
    
    private ClientCallbackRMI renewCallback() throws RemoteException {
        if (this.callback != null) {
            this.destroyCallback();
        }
        this.callback = new GameListCallback();
        return (ClientCallbackRMI) UnicastRemoteObject.exportObject(
                    this.callback, 
                    0
               );
    }


    private void destroyCallback() {
        try {
            UnicastRemoteObject.unexportObject(this.callback, true);
        } catch (NoSuchObjectException e) {
            System.err.println("Couldn't unexport callback; ignoring the error");
        }
        this.callback = null;
        
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