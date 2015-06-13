package rmi_interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerRMI extends Remote {

    public int login(String nick, String password, ClientCallbackRMI notifier)
        throws WrongPasswordException,
               RemoteException;
    
    public void logout(String nick, int cookie)
        throws UserNotLoggedException,
               RemoteException;
    
    public void register(String nick, String password)
        throws UserAlreadyRegisteredException,
               RemoteException;
}
