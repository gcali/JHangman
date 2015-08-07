package jhangmanclient.controller;

import java.rmi.RemoteException;

import development_support.NotImplementedException;
import rmi_interface.RMIServer;
import rmi_interface.UserNotLoggedInException;
import utility.ActionExecutor;

public class GameChooserController {

    private RMIServer server;
    private String nick;
    private int cookie;
    private int logoutAction;

    public GameChooserController(RMIServer server, String nick, int cookie) {
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
        } catch (UserNotLoggedInException e) {
            System.err.println("User not logged in; ignoring the error");
        }
        
    }


    public void handleLogout() throws RemoteException {
        ActionExecutor.getActionExecutor().removeAction(this.logoutAction);
        basicHandleLogout();
    } 
    
    public MasterController openGame() {
        throw new NotImplementedException();
    }
    
    public PlayerController joinGame(String name) {
        throw new NotImplementedException();
    }

}