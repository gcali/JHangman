package jhangmanserver.remote.tcp;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.UUID;

import jhangmanserver.address.MulticastAddressGenerator;
import jhangmanserver.game_data.AbortedGameEvent;
import jhangmanserver.game_data.GameFullEvent;
import jhangmanserver.game_data.GameListHandler;
import jhangmanserver.remote.rmi.LoggedInChecker;
import tcp_interface.answers.OpenGameAnswer;
import tcp_interface.answers.OpenGameCompletedAnswer;
import tcp_interface.requests.OpenGameRequest;
import tcp_interface.requests.Request;
import utility.Cleaner;
import utility.observer.JHObserver;
import utility.observer.ObservationHandler;

class OpenGameHandler extends TCPHandler {
    
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
            boolean shouldAbort = false;
            if (!loggedInChecker.isLoggedIn(gameName,
                                                 request.getCookie())) {
                this.printMessage("User wasn't logged in");
                shouldAbort = true;
            } 
            if (gameListHandler.isGameOpen(gameName)) {
                this.printMessage("Game was already open");
                shouldAbort = true;
            } 
            
            if (request.getPlayers() <= 0) {
                this.printMessage("Can't have non-positive players");
                shouldAbort = true;
            }
            
            if (shouldAbort) {
                outputStream.writeObject(new OpenGameAnswer(false));
                return;
            }
            String key = UUID.randomUUID().toString();
            this.printMessage("Creating confirmer...");
            OpenGameConfirmer confirmer = new OpenGameConfirmer(
                gameName,
                this.getTimeout(),
                key, 
                outputStream, 
                inputStream,
                socket
            );
            this.printMessage("Confirmer created");
            try (
                Cleaner cleaner = gameListHandler.openGame(gameName,
                                                           request.getPlayers(),
                                                           confirmer)
            ) {
                outputStream.writeObject(new OpenGameAnswer(true));
                OpenGameData gameData = confirmer.handleConfirmation();
                if (gameData == null) {
                    printMessage("Aborting game");
                    gameListHandler.abortUserGames(gameName);
                } else {
                    printMessage("Here's the address! " + gameData.getAddress());
                    printMessage("Here's the port: " + gameData.getPort());
                    printMessage("Setting key and address!");
                    gameListHandler.setKeyAddressPort(
                        gameName, 
                        key, 
                        gameData.getAddress(), 
                        gameData.getPort()
                    );
                } 
            }
        } catch (IOException e) {
            
        }
    }
    
    private static class OpenGameData {
        private final InetAddress address;
        private final int port;
        
        public OpenGameData(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        public InetAddress getAddress() {
            return address;
        }

        public int getPort() {
            return port;
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
        
        public OpenGameData handleConfirmation() {
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

        private OpenGameData handleTimeout() {
            printMessage("Handling timeout");
            this.sendAbort();
            return null;
        }

        private OpenGameData handleEventOrInputClosed() {
            synchronized(this.eventLock) {
                switch (this.eventId) {
                case FULL:
                    printMessage("Game full!");
                    InetAddress address = 
                        MulticastAddressGenerator.getAddress("239.255.0.0/16");
                    if (address == null) {
                        System.out.println("Couldn't find a free address");
                        return null;
                    }
                    int port =
                        MulticastAddressGenerator.getFreePort();
                    if (port < 0) {
                        printError("Couldn't find a free port, falling back on" +
                                   " a used one");
                        port = MulticastAddressGenerator.getRandomPort();
                        MulticastAddressGenerator.freeAddress(address);
                        return null;
                    }
                    OpenGameData gameData = new OpenGameData(address, port);
                    printMessage("Generated address: " + address);
                    try {
                        this.output.writeObject(
                                OpenGameCompletedAnswer.createAccepted(address, 
                                                                       port,
                                                                       this.key)
                        );
                        return gameData;
                    } catch (IOException e) {
                        MulticastAddressGenerator.freeAddress(address);
                        MulticastAddressGenerator.freePort(port);
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
