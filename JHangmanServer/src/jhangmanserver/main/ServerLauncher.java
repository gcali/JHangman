package jhangmanserver.main;

import java.rmi.RemoteException;

import rmi_interface.RMIServer;
import jhangmanserver.game_data.GameListHandler;
import jhangmanserver.remote.ConcurrentRMIServer;
import jhangmanserver.remote.ConcurrentTCPServer;

public class ServerLauncher {

    public static void main(String[] args) {
        
        int port = RMIServer.defaultPort;
        String name = RMIServer.name;
        try {
            port = Integer.parseInt(args[0]);
            name = args[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            
        }
        GameListHandler gameListHandler = new GameListHandler();
        ConcurrentRMIServer server = new ConcurrentRMIServer(gameListHandler);
        
        try {
            server.export(name,port);
        } catch (RemoteException e) {
            System.err.println("Couldn't launch the server; connection error");
            throw new RuntimeException(e);
        }
        
        try {
            new ConcurrentTCPServer(server, gameListHandler).run(); 
        } catch (Throwable t) {
            server.unexport();
        } 
    } 
}
