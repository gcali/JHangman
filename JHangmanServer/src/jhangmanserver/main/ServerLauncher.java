package jhangmanserver.main;

import java.rmi.RemoteException;

import rmi_interface.RMIServer;
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
        ConcurrentRMIServer server = new ConcurrentRMIServer();
        
        try {
            server.export(name,port);
        } catch (RemoteException e) {
            System.err.println("Couldn't launch the server; connection error");
            throw new RuntimeException(e);
        }
        
        try {
            new ConcurrentTCPServer(server).run(); 
        } catch (Throwable t) {
            server.unexport();
        } 
    } 
}
