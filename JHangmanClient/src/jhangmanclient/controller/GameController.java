package jhangmanclient.controller;

import java.rmi.RemoteException;

import rmi_interface.RMIServer;
import rmi_interface.UserNotLoggedException;
import utility.ActionExecutor;

public class GameController {

    private RMIServer server;
    private String nick;
    private int cookie;
    private int logoutAction;

    public GameController(RMIServer server, String nick, int cookie) {
        this.server = server;
        this.nick = nick;
        this.cookie = cookie;
        this.logoutAction = ActionExecutor.getActionExecutor().addAction(
                new Runnable() { 
            @Override
            public void run() {
                try {
                    basicHandleLogout(); 
                } catch (Exception e) {
                    
                }
            }
        });
    }
    

    private void basicHandleLogout() throws RemoteException {
        try {
            this.server.logOut(this.nick, this.cookie);
        } catch (RemoteException e) {
            throw e;
        } catch (UserNotLoggedException e) {
            System.err.println("User not logged in; ignoring the error");
        }
        
    }


    public void handleLogout() throws RemoteException {
        ActionExecutor.getActionExecutor().removeAction(this.logoutAction);
        basicHandleLogout();
    } 

}