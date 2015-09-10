package jhangmanserver.main;

import java.rmi.RemoteException;

import jhangmanserver.game_data.GameListHandler;
import jhangmanserver.remote.rmi.ConcurrentRMIServer;
import jhangmanserver.remote.tcp.ConcurrentTCPServer;
import rmi_interface.RMIServer;

public class ServerLauncher {

    public static void main(String[] args) {
        
        boolean shouldEncrypt = true;
        int port = RMIServer.defaultPort;
        String hostName = "localhost";
        String name = RMIServer.name;
        try {
            shouldEncrypt = Boolean.parseBoolean(args[0]);
            port = Integer.parseInt(args[1]);
            hostName = args[2];
            name = args[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            
        }
        
        System.setProperty("java.rmi.server.hostname", hostName);
        GameListHandler gameListHandler = new GameListHandler();
        ConcurrentRMIServer server = new ConcurrentRMIServer(gameListHandler, shouldEncrypt);
        
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
