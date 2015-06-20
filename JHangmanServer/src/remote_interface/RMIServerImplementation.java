package remote_interface;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import rmi_interface.ClientCallbackRMI;
import rmi_interface.RMIServer;
import rmi_interface.UserAlreadyLoggedInException;
import rmi_interface.UserAlreadyRegisteredException;
import rmi_interface.UserNotLoggedException;
import rmi_interface.WrongPasswordException;
import users.User;

public class RMIServerImplementation implements RMIServer {
    
    private Map<String,User> userData = new HashMap<String, User>(); 
	private Registry registry = null;
	private static AtomicInteger cookies = new AtomicInteger();

    private static void bind(RMIServer stub, int port, Registry registry) throws RemoteException {
        try {
            registry.bind(RMIServer.name, stub);
        } catch (AlreadyBoundException e) {
            try {
                registry.unbind(RMIServer.name);
                registry.bind(RMIServer.name, stub);
            } catch (NotBoundException | AlreadyBoundException e1) {
            } 
        }
    }
    
    public void export(int port) throws RemoteException {
        try {
            this.registry = LocateRegistry.getRegistry(port); 
        } catch (RemoteException e) {
            this.registry = LocateRegistry.createRegistry(port);
        }
        RMIServer stub = (RMIServer) UnicastRemoteObject.exportObject(this, port);
        bind(stub, port, this.registry);
    }

    @Override
    public int login(String nick, String password, ClientCallbackRMI notifier)
            throws WrongPasswordException, RemoteException, UserAlreadyLoggedInException {
        User user = null;
        synchronized(this.userData){
            user = this.userData.get(nick);
            if (user == null) {
                throw new WrongPasswordException("User data incorrect");
            } 
        }
        int cookie;
        synchronized(user) {
            if (!user.isPasswordCorrect(password)) {
                throw new WrongPasswordException("User data incorrect");
            } else if (user.isLoggedIn()) {
                throw new UserAlreadyLoggedInException();
            }
            cookie = this.cookies.getAndIncrement();
            user.setCookie(cookie);
            user.setCallback(notifier);
            user.setLoggedIn(true);
        }
        return cookie;
    }

    @Override
    public void logout(String nick, int cookie) throws UserNotLoggedException,
            RemoteException {
        User user = null;
        synchronized(this.userData){
            user = this.userData.get(nick);
        }
        if (user == null) {
            throw new UserNotLoggedException();
        }
        synchronized(user) {
            if (user.isLoggedIn() && user.checkCookie(cookie)) {
                user.removeCallback();
                user.setLoggedIn(false);
            } else {
                throw new UserNotLoggedException();
            }
        } 
    }

    @Override
    public void register(String nick, String password)
            throws UserAlreadyRegisteredException, RemoteException {
        synchronized(this.userData) {
            if (this.userData.containsKey(nick)) {
                throw new UserAlreadyRegisteredException(nick);
            }
            User user = new User(nick, password);
            this.userData.put(nick, user);
        }
    }

}