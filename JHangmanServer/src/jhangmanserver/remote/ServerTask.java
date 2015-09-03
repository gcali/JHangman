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
import java.util.concurrent.atomic.AtomicInteger;

import jhangmanserver.game_data.AbortedGameEvent;
import jhangmanserver.game_data.GameListHandler;
import tcp_interface.answers.OpenGameAnswer;
import tcp_interface.answers.OpenGameCompletedAnswer;
import tcp_interface.requests.JoinGameRequest;
import tcp_interface.requests.OpenGameRequest;
import tcp_interface.requests.Request;
import utility.JHObjectInputStream;
import utility.JHObjectOutputStream;
import utility.observer.JHObserver;
import utility.observer.ObservationHandler;


public class ServerTask implements Runnable {
    
    private static final AtomicInteger idGenerator = new AtomicInteger();
    private static final int OPEN_GAME_TIMEOUT = 300000;
    private Socket socket;
    private GameListHandler gameListHandler;
    private LoggedInChecker loggedInChecker;
    private final int id;

    public ServerTask(Socket socket, 
                      GameListHandler gameListHandler,
                      LoggedInChecker loggedInChecker) {
        this.socket = socket;
        this.gameListHandler = gameListHandler;
        this.loggedInChecker = loggedInChecker;
        this.id = idGenerator.getAndIncrement();
    }
    
    private void printMessage(String message) {
        System.out.println(buildMessageString(message));
    }
    
    private void printError(String message) {
        System.err.println(buildMessageString(message));
    }
    
    private String getPrefix() {
        return String.format("[Server %2d] ", this.id);
    }
    
    private String buildMessageString(String message) {
        return getPrefix() + message;
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
            this.handleJoinGame((JoinGameRequest) request,
                                outputStream,
                                inputStream);
            break;
        case OPEN_GAME:
            this.printMessage("Open game request");
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
            this.printMessage("Starting to handle open game");
            String gameName = request.getNick();
            if (!this.loggedInChecker.isLoggedIn(gameName,
                                                 request.getCookie())) {
                this.printMessage("User wasn't logged in");
                outputStream.writeObject(new OpenGameAnswer(false));
                return;
            } 
            if (this.gameListHandler.isGameOpen(gameName)) {
                this.printMessage("Game was already open");
                outputStream.writeObject(new OpenGameAnswer(false));
                return;
            } 
            String key = UUID.randomUUID().toString();
            this.printMessage("Creating confirmer...");
            OpenGameConfirmer confirmer = 
                    new OpenGameConfirmer(
                            gameName,
                            OPEN_GAME_TIMEOUT, 
                            key, 
                            outputStream, 
                            inputStream,
                            this.socket
                    );
            this.printMessage("Confirmer created");
            this.gameListHandler.openGame(gameName,
                                          request.getPlayers(),
                                          confirmer);
            outputStream.writeObject(new OpenGameAnswer(true));
            InetAddress address = confirmer.handleConfirmation();
            if (address == null) {
                this.gameListHandler.abortGame(gameName);
            } else {
                this.gameListHandler.setKeyAddress(gameName, key, address);
            }
        } catch (IOException e) {
            
        }
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

        private int timeout;
        private String key;
        private ObjectOutputStream output;
        private ObjectInputStream input;
        private Socket socket;
        private String name;

        private EventID eventId = EventID.NOT_HANDLED;
        
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
            printMessage("Starting handling confirmation");
            try {
                this.socket.setSoTimeout(this.timeout);
            } catch (SocketException e) {
                printError("Couldn't set socket timeout");
                return null;
            }
            try {
                printMessage("Getting request");
                Request request = getRequest(this.input);
                printMessage("Got request");
                switch (request.getId()) {
                case ABORT:
                    printMessage("Request was abort");
                    return null;
                default:
                    throw new IOException("Protocol violated, "+ 
                                          "invalid message received"); 
                }
            } catch (EOFException e) {
                printMessage("Got event or client closed connection");
                return this.handleEventOrInputClosed();
            } catch (SocketTimeoutException e) {
                printMessage("Got timeout");
                return handleTimeout();
            } catch (IOException e) {
                printError("Got IOException??");
            }
            return null;
        }
        
        private void sendAbort() {
            try {
                this.output.writeObject(
                        OpenGameCompletedAnswer.createAborted()
                );
                printMessage("Abort answer sent");
            } catch (IOException e1) {
                printError("Couldn't send abort notification to " + this.name);
            } 
        }

        private InetAddress handleTimeout() {
            printMessage("Handling timeout");
            this.sendAbort();
            return null;
        }

        private InetAddress handleEventOrInputClosed() {
            synchronized(this.eventId) {
                switch (this.eventId) {
                case FULL:
                    printMessage("Game full!");
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
                case ABORT:
                    printMessage("Game aborted");
                    this.sendAbort();
                    return null;
                case NOT_HANDLED:
                    printMessage("Client closed connection");
                    return null;
                default:
                    assert false;
                    return null;
                }
            }
        }

        @ObservationHandler
        public void onGameFull(GameFullEvent event) {
            synchronized(this.eventId) {
                if (this.eventId == EventID.NOT_HANDLED) {
                    this.eventId = EventID.FULL;
                    try {
                        this.socket.shutdownInput();
                    } catch (IOException e) {
                    }
                }
                
            }
        } 
        
        @ObservationHandler
        public void onGameAborted(AbortedGameEvent event) {
            synchronized(this.eventId) {
                if (this.eventId == EventID.NOT_HANDLED) {
                    this.eventId = EventID.ABORT;
                    try {
                        this.socket.shutdownInput();
                    } catch (IOException e) {
                    }
                }
            }
        }
        
    }
    private enum EventID {
        NOT_HANDLED, FULL, ABORT;
    }

}