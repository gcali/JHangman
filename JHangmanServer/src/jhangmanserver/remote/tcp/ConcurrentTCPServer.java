package jhangmanserver.remote.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import jhangmanserver.address.AddressRange;
import jhangmanserver.game_data.GameListHandler;
import jhangmanserver.remote.rmi.LoggedInChecker;


public class ConcurrentTCPServer implements Runnable {

    private LoggedInChecker loggedInChecker;
    private ServerSocket mainSocket;
    private ThreadPoolExecutor threadPool;
    private boolean done = false;
    private GameListHandler gameListHandler;
    private AddressRange addressRange;

    public ConcurrentTCPServer(LoggedInChecker loggedInChecker, 
                               GameListHandler gameListHandler,
                               int port,
                               AddressRange addressRange) 
                                       throws IOException {
        this.loggedInChecker = loggedInChecker;
        this.gameListHandler = gameListHandler;
        this.addressRange = addressRange;
        this.mainSocket = new ServerSocket(port);
        this.threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        while (!done) {
            try {
                Socket socket = this.mainSocket.accept();
                ServerTask task = new ServerTask(socket, 
                                                 this.gameListHandler,
                                                 this.loggedInChecker,
                                                 this.addressRange);
                this.threadPool.execute(task);
            } catch (IOException e) {
                System.err.println("Error during socket acceptance; ignoring");
            }
        }
    } 
}