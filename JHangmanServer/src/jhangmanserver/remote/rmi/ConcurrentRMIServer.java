package jhangmanserver.remote.rmi;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jhangmanserver.game_data.GameListHandler;
import jhangmanserver.users.User;
import rmi_interface.ClientCallbackRMI;
import rmi_interface.RMIServer;
import rmi_interface.UserAlreadyLoggedInException;
import rmi_interface.UserAlreadyRegisteredException;
import rmi_interface.UserNotLoggedInException;
import rmi_interface.WrongPasswordException;

/**
 * Implementazione delle funzionalità offerte tramite RMI server side;
 * la classe implementa anche il {@link LoggedInChecker}.
 * @author gcali
 *
 */
public class ConcurrentRMIServer implements RMIServer, LoggedInChecker {
    private Map<String,User> userData = new ConcurrentHashMap<String, User>(); 
	private Registry registry = null;
    private String bindingName;
    private GameListHandler gameListHandler;
	private static AtomicInteger cookies = new AtomicInteger();
    private boolean shouldEncrypt;
	
    public ConcurrentRMIServer(GameListHandler gameListHandler) {
        this(gameListHandler, true);
    }

	public ConcurrentRMIServer(
	    GameListHandler gameListHandler, 
	    boolean shouldEncrypt
	) {
	    this.gameListHandler = gameListHandler;
        this.shouldEncrypt = shouldEncrypt;
	    try {
	        //TODO remove test accounts
            this.register("Gio", "test");
            this.register("Mike", "test");
            this.register("Phil", "test");
        } catch (RemoteException e) {
            assert false;
        } catch (UserAlreadyRegisteredException e) {
            assert false;
        }
	}

    public void export(String bindingName, int port) throws RemoteException {
        System.out.println("Exporting to " + port);
        if (this.registry == null) {
                this.registry = LocateRegistry.createRegistry(port);
        }
        RMIServer stub = (RMIServer) UnicastRemoteObject.exportObject(
            this, port);
        System.out.println("Stub exported");
        try {
            System.out.println("Binding...");
            this.registry.bind(bindingName, stub);
            this.bindingName = bindingName;
            System.out.println("Bound!");
        } catch (AlreadyBoundException e) { 
            System.out.println("Stub was already bound");
            this.registry.rebind(bindingName, stub);
        }
    }
    
    public void unexport() {
        try {
            this.registry.unbind(this.bindingName);
            UnicastRemoteObject.unexportObject(this, true);
        } catch (RemoteException | NotBoundException e) {
            System.err.println("Unexport failed; ignoring errors");
        }
    }

    @Override
    public int logIn(String nick, String password, ClientCallbackRMI notifier)
            throws WrongPasswordException, RemoteException, UserAlreadyLoggedInException {
        User user = getUser(nick);
        if (user == null || password == null) {
            throw new WrongPasswordException("User data incorrect");
        }
        int cookie;
        synchronized(user) {
            if (!user.isPasswordCorrect(password)) {
                throw new WrongPasswordException("User data incorrect");
            } else if (user.isLoggedIn()) {
                throw new UserAlreadyLoggedInException();
            }
            cookie = cookies.getAndIncrement();
            user.setCookie(cookie);
            user.setLoggedIn(true);
            gameListHandler.addCallback(nick, notifier);
        }
        return cookie;
    }
    
    public int forceLogIn(
        String nick, 
        String password, 
        ClientCallbackRMI notifier
    ) throws WrongPasswordException, RemoteException { 
        try {
            this.logOut(nick, 0, true);
        } catch (UserNotLoggedInException e) {
            //shouldn't happen, in any case safe to ignore
        }
        try {
            return logIn(nick, password, notifier);
        } catch (UserAlreadyLoggedInException e) {
            return forceLogIn(nick, password, notifier);
        }
    }

    private User getUser(String nick) { 
        return this.userData.get(nick);
    }
    @Override
    public void logOut(String nick, int cookie) throws UserNotLoggedInException,
            RemoteException {
        this.logOut(nick, cookie, false);
    }
    
    private void logOut(String nick, int cookie, boolean override)
        throws UserNotLoggedInException {
        User user = getUser(nick);

        if (user == null) {
            throw new UserNotLoggedInException();
        } 

        synchronized(user) {
            if (
                user.isLoggedIn() && 
                (override || user.checkCookie(cookie))
            ) {
                this.gameListHandler.abortUserGames(nick);
                user.logOut();
                this.gameListHandler.removeCallback(nick);
            } else {
                throw new UserNotLoggedInException();
            }
        } 
        
    }
    
    @Override
    public void register(String nick, String password)
            throws UserAlreadyRegisteredException, RemoteException {
        User user = new User(nick, password, this.shouldEncrypt);
        User oldUser = this.userData.putIfAbsent(nick, user);
        if (oldUser != null) {
            throw new UserAlreadyRegisteredException(nick + " already logged in");
        }
    }
    
    public boolean isLoggedIn(String nick, int cookie) {
        User user = this.userData.get(nick);
        if (user == null) {
            return false;
        } else {
            synchronized(user) {
                return user.checkCookie(cookie) && user.isLoggedIn();
            }
        }
    } 
}