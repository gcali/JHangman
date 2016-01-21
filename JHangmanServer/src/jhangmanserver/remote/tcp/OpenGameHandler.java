package jhangmanserver.remote.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.UUID;

import jhangmanserver.address.AddressRange;
import jhangmanserver.game_data.GameListHandler;
import jhangmanserver.remote.rmi.LoggedInChecker;
import tcp_interface.answers.OpenGameAnswer;
import tcp_interface.requests.OpenGameRequest;
import utility.Cleaner;
import utility.Loggable;

class OpenGameHandler implements Loggable {
    
    private String id;

    public OpenGameHandler(int id) {
        this.id = String.format("OpenGameHandler %3d", id);
    }
    
    @Override
    public String getLoggableId() {
        return this.id;
    }
    
    void handleOpenGame(
        OpenGameRequest request, 
        ObjectOutputStream outputStream, 
        ObjectInputStream inputStream,
        Socket socket,
        LoggedInChecker loggedInChecker,
        GameListHandler gameListHandler, 
        AddressRange addressRange
    ) {
        try {
            this.printDebugMessage("Starting to handle open game");
            String gameName = request.getNick();
            boolean shouldAbort = false;
            if (!loggedInChecker.isLoggedIn(gameName,
                                                 request.getCookie())) {
                this.printDebugMessage("User wasn't logged in");
                shouldAbort = true;
            } 
            if (gameListHandler.isGameOpen(gameName)) {
                this.printDebugMessage("Game was already open");
                shouldAbort = true;
            } 
            
            if (request.getPlayers() <= 0) {
                this.printDebugMessage("Can't have non-positive players");
                shouldAbort = true;
            }
            
            if (shouldAbort) {
                outputStream.writeObject(new OpenGameAnswer(false));
                return;
            }
            String key = UUID.randomUUID().toString();
            this.printDebugMessage("Creating confirmer...");
            OpenGameConfirmer confirmer = new OpenGameConfirmer(
                gameName,
                key, 
                addressRange,
                outputStream, 
                inputStream,
                socket
            );
            this.printDebugMessage("Confirmer created");
            try (
                Cleaner cleaner = gameListHandler.openGame(gameName,
                                                           request.getPlayers(),
                                                           confirmer)
            ) {
                outputStream.writeObject(new OpenGameAnswer(true));
                OpenGameData gameData = confirmer.handleConfirmation();
                printDebugMessage("Confirmer passed this gameData: " + gameData);
                if (gameData == null) {
                    this.printDebugMessage("Aborting game");
                    gameListHandler.abortUserGames(gameName);
                } else {
                    this.printDebugMessage("Here's the address! " + gameData.getAddress());
                    this.printDebugMessage("Here's the port: " + gameData.getPort());
                    this.printDebugMessage("Setting key and address!");
                    gameListHandler.setKeyAddressPort(
                        gameName, 
                        key, 
                        gameData.getAddress(), 
                        gameData.getPort()
                    );
                } 
            }
        } catch (IOException e) {
            
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                this.printError("Couldn't close socket");
            }
        }
    } 

    static class OpenGameData {
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
    
}