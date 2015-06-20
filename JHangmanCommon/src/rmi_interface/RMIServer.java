package rmi_interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

    public interface RMIServer extends Remote {
        
        public static final String name = "hangman_server_rmi_name";
    
        public int login(String nick, String password, ClientCallbackRMI notifier)
            throws UserAlreadyLoggedInException,
                   WrongPasswordException,
                   RemoteException;
        
        public void logout(String nick, int cookie)
            throws UserNotLoggedException,
                   RemoteException;
        
        public void register(String nick, String password)
            throws UserAlreadyRegisteredException,
                   RemoteException;
    }
