package jhangmanserver.main;

import java.rmi.RemoteException;

import jhangmanserver.config.ConfigData;
import jhangmanserver.game_data.GameListHandler;
import jhangmanserver.remote.rmi.ConcurrentRMIServer;
import jhangmanserver.remote.tcp.ConcurrentTCPServer;

public class ServerLauncher {

    public static void main(String[] args) {
        
        ConfigData config = new ConfigData();
        try {
            config.setShouldEncrypt(Boolean.parseBoolean(args[0]));
            config.setRMIPort(Integer.parseInt(args[1]));
            config.setHostName(args[2]);
            config.setName(args[3]);
            config.setTCPPort(Integer.parseInt(args[4]));
        } catch (ArrayIndexOutOfBoundsException e) {
            
        }
        
        System.setProperty("java.rmi.server.hostname", config.getHostName());
        GameListHandler gameListHandler = new GameListHandler();
        ConcurrentRMIServer server = new ConcurrentRMIServer(
            gameListHandler, 
            config.getShouldEncrypt()
        );
        
        try {
            server.export(config.getName(),config.getRMIPort());
        } catch (RemoteException e) {
            System.err.println("Couldn't launch the server; connection error");
            throw new RuntimeException(e);
        }
        
        try {
            new ConcurrentTCPServer(
                server, 
                gameListHandler, 
                config.getTCPPort(),
                config.getAddressRange()
            ).run(); 
        } catch (Throwable t) {
            server.unexport();
        } 
    } 
}
