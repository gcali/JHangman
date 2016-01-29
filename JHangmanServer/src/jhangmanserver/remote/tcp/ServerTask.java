package jhangmanserver.remote.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import jhangmanserver.address.AddressRange;
import jhangmanserver.game_data.GameListHandler;
import jhangmanserver.remote.rmi.LoggedInChecker;
import tcp_interface.requests.JoinGameRequest;
import tcp_interface.requests.OpenGameRequest;
import tcp_interface.requests.Request;
import utility.JHObjectInputStream;
import utility.JHObjectOutputStream;
import utility.Loggable;


public class ServerTask extends TCPHandler implements Runnable, Loggable {
    
    private static final AtomicInteger idGenerator = new AtomicInteger();
    private Socket socket;
    private GameListHandler gameListHandler;
    private LoggedInChecker loggedInChecker;
    private int id;
    private AddressRange addressRange;

    public ServerTask(Socket socket, 
                      GameListHandler gameListHandler,
                      LoggedInChecker loggedInChecker,
                      AddressRange addressRange) {
        this.id = idGenerator.getAndIncrement(); //debug ID
        this.socket = socket;
        this.gameListHandler = gameListHandler;
        this.loggedInChecker = loggedInChecker;
        this.addressRange = addressRange;
    }
    
    @Override
    public void run() {
        this.printDebugMessage("Starting task");
        try (
            ObjectOutputStream outputStream = 
                new JHObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream =
                new JHObjectInputStream(socket.getInputStream());
        ) {
            
            this.printDebugMessage("Getting request");
            Request request = getRequest(inputStream); 
            this.printDebugMessage("Got request");
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
        
        this.printDebugMessage("Starting request handling");
        switch (request.getId()) {
        case JOIN_GAME:
            this.printDebugMessage("Join game request");
            new JoinGameHandler(this.id).handleJoinGame(
                (JoinGameRequest) request, 
                outputStream, 
                inputStream, 
                this.socket,
                this.loggedInChecker, 
                this.gameListHandler
            );
            break;
        case OPEN_GAME:
            this.printDebugMessage("Open game request");
            new OpenGameHandler(this.id).handleOpenGame(
                (OpenGameRequest) request, 
                outputStream, 
                inputStream, 
                this.socket, 
                this.loggedInChecker, 
                this.gameListHandler,
                this.addressRange
            );
            break;
        default:
            break;
        }
    }

    @Override
    public String getLoggableId() {
        return String.format("ServerTask %3d", this.id);
    } 
}