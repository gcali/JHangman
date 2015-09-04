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

import jhangmanserver.game_data.AbortedGameEvent;
import jhangmanserver.game_data.GameListHandler;
import tcp_interface.answers.OpenGameAnswer;
import tcp_interface.answers.OpenGameCompletedAnswer;
import tcp_interface.requests.OpenGameRequest;
import tcp_interface.requests.Request;
import utility.observer.JHObserver;
import utility.observer.ObservationHandler;

class OpenGameHandler extends TCPHandler {
    
    private static final int OPEN_GAME_TIMEOUT = 0;
    private int id;

    public OpenGameHandler(int id) {
        super(id);
    }
    
    void handleOpenGame(
        OpenGameRequest request, 
        ObjectOutputStream outputStream, 
        ObjectInputStream inputStream,
        Socket socket,
        LoggedInChecker loggedInChecker,
        GameListHandler gameListHandler
    ) {
        try {
            this.printMessage("Starting to handle open game");
            String gameName = request.getNick();
            if (!loggedInChecker.isLoggedIn(gameName,
                                                 request.getCookie())) {
                this.printMessage("User wasn't logged in");
                outputStream.writeObject(new OpenGameAnswer(false));
                return;
            } 
            if (gameListHandler.isGameOpen(gameName)) {
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
                            socket
                    );
            this.printMessage("Confirmer created");
            gameListHandler.openGame(gameName,
                                     request.getPlayers(),
                                     confirmer);
            outputStream.writeObject(new OpenGameAnswer(true));
            InetAddress address = confirmer.handleConfirmation();
            if (address == null) {
                gameListHandler.abortGame(gameName);
            } else {
                gameListHandler.setKeyAddress(gameName, key, address);
            }
        } catch (IOException e) {
            
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
        private final Object eventLock = new Object();
        
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
            synchronized(this.eventLock) {
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
            synchronized(this.eventLock) {
                if (this.eventId == EventID.NOT_HANDLED) {
                    this.eventId = EventID.FULL;
                }
                
            }
            try {
                this.socket.shutdownInput();
            } catch (IOException e) {
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
}
