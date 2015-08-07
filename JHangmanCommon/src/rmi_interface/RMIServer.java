package rmi_interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

    public interface RMIServer extends Remote {
        
        public static final String name = "hangman_server_rmi_name";
        public static final int defaultPort = 55544;
    
        public int logIn(String nick, 
                         String password, 
                         ClientCallbackRMI notifier)
            throws UserAlreadyLoggedInException,
                   WrongPasswordException,
                   RemoteException;

        public int forceLogIn(String nick, 
                              String password, 
                              ClientCallbackRMI notifier)
            throws WrongPasswordException,
                   RemoteException;
        
        public void logOut(String nick, int cookie)
            throws UserNotLoggedInException,
                   RemoteException;
        
        public void register(String nick, String password)
            throws UserAlreadyRegisteredException,
                   RemoteException;
    }
