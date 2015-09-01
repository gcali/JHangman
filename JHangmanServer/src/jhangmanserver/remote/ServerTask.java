package jhangmanserver.remote;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import tcp_interface.answers.OpenGameAnswer;
import tcp_interface.requests.JoinGameRequest;
import tcp_interface.requests.OpenGameRequest;
import tcp_interface.requests.Request;


public class ServerTask implements Runnable {
    
    private Socket socket;
    private GameListHandler gameListHandler;
    private LoggedInChecker loggedInChecker;

    public ServerTask(Socket socket, 
                      GameListHandler gameListHandler,
                      LoggedInChecker loggedInChecker) {
        this.socket = socket;
        this.gameListHandler = gameListHandler;
        this.loggedInChecker = loggedInChecker;
    }

    @Override
    public void run() {
        try (
            ObjectOutputStream outputStream = 
                    new ObjectOutputStream(this.socket.getOutputStream());
            ObjectInputStream inputStream = 
                    new ObjectInputStream(this.socket.getInputStream());
        ) {
            
            Request request = getRequest(inputStream); 
            this.handleRequest(request, outputStream, inputStream);
        } catch (IOException e) {
            System.err.println("Socket state corrupted");
            return;
        }

    }

    private void handleRequest(Request request, 
                               ObjectOutputStream outputStream, 
                               ObjectInputStream inputStream) {
        
        switch (request.getId()) {
        case JOIN_GAME:
            this.handleJoinGame((JoinGameRequest) request,
                                outputStream,
                                inputStream);
            break;
        case OPEN_GAME:
            this.handleOpenGame((OpenGameRequest) request,
                                outputStream,
                                inputStream);
            break;
        default:
            break;
        }
    }


    private void handleJoinGame(JoinGameRequest request, 
                                ObjectOutputStream outputStream, 
                                ObjectInputStream inputStream) {
        // TODO Auto-generated method stub
        
    }

    private void handleOpenGame(OpenGameRequest request, 
                                ObjectOutputStream outputStream, 
                                ObjectInputStream inputStream) {
        try {
            if (!this.loggedInChecker.isLoggedIn(request.getNick(), 
                                                 request.getCookie())) {
                outputStream.writeObject(new OpenGameAnswer(false));
                return;
            } 
            if (this.gameListHandler.isGameOpen(request.getNick())) {
                outputStream.writeObject(new OpenGameAnswer(false));
                return;
            } 
            this.openGameAndAnswer(request.getNick(), 
                                   request.getPlayers(), 
                                   outputStream);
            //TODO handle game opening completion
        } catch (IOException e) {
            
        }
    }

    private void openGameAndAnswer(String nick,
                                   int players,
                                   ObjectOutputStream outputStream) throws IOException {
        this.gameListHandler.openGame(nick, players);
        outputStream.writeObject(new OpenGameAnswer(true));
    }

    private static Request getRequest(ObjectInputStream inputStream) 
            throws IOException {
        try {
            return (Request) inputStream.readObject();
        } catch (ClassNotFoundException e) {
            System.err.println("Internal protocol violated during " +
                               "TCP communication");
            throw new IOException(e);
        }
    }

}