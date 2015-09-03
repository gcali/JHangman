package jhangmanserver.remote;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import jhangmanserver.game_data.GameListHandler;
import tcp_interface.answers.OpenGameAnswer;
import tcp_interface.answers.OpenGameCompletedAnswer;
import tcp_interface.requests.JoinGameRequest;
import tcp_interface.requests.OpenGameRequest;
import tcp_interface.requests.Request;
import utility.observer.JHObserver;
import utility.observer.ObservationHandler;


public class ServerTask implements Runnable {
    
    private static final int OPEN_GAME_TIMEOUT = 300;
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
            String gameName = request.getNick();
            if (!this.loggedInChecker.isLoggedIn(gameName,
                                                 request.getCookie())) {
                outputStream.writeObject(new OpenGameAnswer(false));
                return;
            } 
            if (this.gameListHandler.isGameOpen(gameName)) {
                outputStream.writeObject(new OpenGameAnswer(false));
                return;
            } 
            String key = UUID.randomUUID().toString();
            OpenGameConfirmer confirmer = 
                    new OpenGameConfirmer(
                            gameName,
                            OPEN_GAME_TIMEOUT, 
                            key, 
                            outputStream, 
                            inputStream,
                            this.socket
                    );
            this.openGameAndAnswer(gameName, 
                                   request.getPlayers(), 
                                   outputStream,
                                   confirmer);
            InetAddress address = confirmer.handleConfirmation();
            if (address == null) {
                this.gameListHandler.abortGame(gameName);
            } else {
                this.gameListHandler.setKeyAddress(gameName, key, address);
            }
        } catch (IOException e) {
            
        }
    }
    
    private void openGameAndAnswer(String nick,
                                   int players,
                                   ObjectOutputStream outputStream,
                                   JHObserver observer) throws IOException {
        this.gameListHandler.openGame(nick, players,observer);
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

    private class OpenGameConfirmer implements JHObserver {
        private AtomicBoolean eventHandled = new AtomicBoolean(false);

        private int timeout;
        private String key;
        private ObjectOutputStream output;
        private ObjectInputStream input;
        private Socket socket;
        private String name;
        
        public OpenGameConfirmer(String name,
                             int timeout, 
                             String key,
                             ObjectOutputStream output,
                             ObjectInputStream input,
                             Socket socket) {
            this.name = name;
            this.timeout = timeout;
            this.key = key;
            this.output = output;
            this.input = input;
            this.socket = socket;
        }
        
        public InetAddress handleConfirmation() {
            try {
                this.socket.setSoTimeout(this.timeout);
            } catch (SocketException e) {
                return null;
            }
            try {
                Request request = getRequest(this.input);
                this.eventHandled.set(true);
                switch (request.getId()) {
                case ABORT:
                    return null;
                default:
                    throw new IOException("Protocol violated, "+ 
                                          "invalid message received"); 
                }
            } catch (EOFException e) {
                return this.handleGameFullOrInputClosed();
            } catch (SocketTimeoutException e) {
                return handleTimeout();
            } catch (IOException e) {
            }
            return null;
        }

        private InetAddress handleTimeout() {
            try {
                this.output.writeObject(
                        OpenGameCompletedAnswer.createAborted()
                );
            } catch (IOException e1) {
                System.err.println("Couldn't send abort notification to " +
                                   this.name);
            }
            return null;
        }

        private InetAddress handleGameFullOrInputClosed() {
            if (this.eventHandled.get()) {
                InetAddress address = MulticastAddressGenerator.getAddress();
                try {
                    this.output.writeObject(
                            OpenGameCompletedAnswer.createAccepted(address, 
                                                                   this.key)
                    );
                    return address;
                } catch (IOException e) {
                    MulticastAddressGenerator.freeAddress(address);
                    return null;
                }
            } else {
                return null;
            }
        }

        @ObservationHandler
        public void onGameFull(GameFullEvent event) {
            if (this.eventHandled.compareAndSet(false, true)) {
                try {
                    this.socket.shutdownInput();
                } catch (IOException e) {
                }
            }
        } 
    }

}