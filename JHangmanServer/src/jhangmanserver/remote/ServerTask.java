package jhangmanserver.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import tcp_interface.requests.JoinGameParameters;
import tcp_interface.requests.OpenGameParameters;
import tcp_interface.requests.Request;
import tcp_interface.requests.RequestParameters;
import development_support.NotImplementedException;


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
            
            this.handleRequest(request);
        } catch (IOException e) {
            System.err.println("Socket state corrupted");
            return;
        }

    }

    private void handleRequest(Request request) {
        
        switch (request.getId()) {
        case JOIN_GAME:
            this.handleJoinGame((JoinGameParameters) request.getParameters());
            break;
        case OPEN_GAME:
            this.handleOpenGame((OpenGameParameters) request.getParameters());
            break;
        }
    }


    private void handleJoinGame(JoinGameParameters parameters) {
        // TODO Auto-generated method stub
        
    }

    private void handleOpenGame(OpenGameParameters parameters) {
        // TODO Auto-generated method stub
        
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