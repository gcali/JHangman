package jhangmanclient.server_interface;

import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import jhangmanclient.game.Game;
import rmi_interface.RMIServer;
import rmi_interface.UserAlreadyRegisteredException;
import rmi_interface.WrongPasswordException;
import development_support.LambdaException;
import development_support.NotImplementedException;


@Deprecated
public class ServerInterface { 

	private String nickname;
    private InetAddress address;
    private int port;
    private RMIServer rMIServer;

	public ServerInterface(String nickname, InetAddress address, int port) 
	        throws RemoteException, NotBoundException{
		this.nickname = nickname;
		this.address = address;
		this.port = port;
		this.rMIServer = getServerRMI(address, port);
	}
	
	private interface ConnectionAction<R,E extends Throwable> {
	    public R action() throws E;
	}
	
	private static <E extends Throwable,R> R tryManyTimes(int times, ConnectionAction<R,E> action)
	    throws E {
	    if (times <= 0) {
	        throw new IllegalArgumentException("Expected times > 0");
	    }
	    E lastException = null;
	    boolean done = false;
	    while (times > 0 && !done) {
	        try {
	            return action.action();
	        } catch (Exception e) {
	            times--;
	            lastException = (E) e;
	        }
	    }
	    throw lastException; 
	}

	private static RMIServer getServerRMI(InetAddress parAddress, int parPort) 
	    throws RemoteException, NotBoundException {
	    final InetAddress address = parAddress;
	    final int port = parPort;
	    try {
            final Registry registry = (Registry) tryManyTimes(3, 
                                                              () -> //\
                                            LocateRegistry.getRegistry(address.getHostAddress(), port));
            return (RMIServer) tryManyTimes(3, () -> registry.lookup(RMIServer.name));
	    } catch (RemoteException | NotBoundException e) {
	        throw e;
	    } catch (Exception e) {
	        throw new RuntimeException("Unexpected exception");
	    } 
    }

    public void register(String password)
		throws UserAlreadyRegisteredException, 
			   ConnectionFailureException {
		throw new NotImplementedException();
	}
	
	public void signin(String password)
		throws WrongPasswordException,
			   ConnectionFailureException {
		throw new NotImplementedException();
	}
	
	public void signout()
			throws IllegalStateException,
				   ConnectionFailureException {
		throw new NotImplementedException();
	}
	
	public Game openGame(int players)
		throws IllegalStateException,
		       GameAlreadyExistsException,
			   ConnectionFailureException {
		throw new NotImplementedException();
	}
	
	public Game joinGame(String name)
		throws IllegalStateException,
			   GameNotFoundException,
			   ConnectionFailureException {
		return null;
		
	}
	
}