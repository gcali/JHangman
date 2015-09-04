package jhangmanserver.remote;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import jhangmanserver.game_data.GameListHandler;
import tcp_interface.requests.JoinGameRequest;
import tcp_interface.requests.OpenGameRequest;
import tcp_interface.requests.Request;
import utility.JHObjectInputStream;
import utility.JHObjectOutputStream;


public class ServerTask extends TCPHandler implements Runnable {
    
    private static final AtomicInteger idGenerator = new AtomicInteger();
    private static final int OPEN_GAME_TIMEOUT = 300000;
    private Socket socket;
    private GameListHandler gameListHandler;
    private LoggedInChecker loggedInChecker;

    public ServerTask(Socket socket, 
                      GameListHandler gameListHandler,
                      LoggedInChecker loggedInChecker) {
        super(idGenerator.getAndIncrement());
        this.socket = socket;
        this.gameListHandler = gameListHandler;
        this.loggedInChecker = loggedInChecker;
    }
    
    @Override
    public void run() {
        this.printMessage("Starting task");
        try (
            ObjectOutputStream outputStream = 
                new JHObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream =
                new JHObjectInputStream(socket.getInputStream());
        ) {
            
            this.printMessage("Getting request");
            Request request = getRequest(inputStream); 
            this.printMessage("Got request");
            this.handleRequest(request, outputStream, inputStream);
        } catch (IOException e) {
            System.err.println("Socket state corrupted");
            e.printStackTrace();
            return;
        }

    }

    private void handleRequest(Request request, 
                               ObjectOutputStream outputStream, 
                               ObjectInputStream inputStream) {
        
        this.printMessage("Starting request handling");
        switch (request.getId()) {
        case JOIN_GAME:
            this.printMessage("Join game request");
            new JoinGameHandler(this.getID()).handleJoinGame(
                (JoinGameRequest) request, 
                outputStream, 
                inputStream, 
                this.socket,
                this.loggedInChecker, 
                this.gameListHandler
            );
            break;
        case OPEN_GAME:
            this.printMessage("Open game request");
            new OpenGameHandler(this.getID()).handleOpenGame(
                (OpenGameRequest) request, 
                outputStream, 
                inputStream, 
                this.socket, 
                this.loggedInChecker, 
                this.gameListHandler
            );
            break;
        default:
            break;
        }
    } 
}