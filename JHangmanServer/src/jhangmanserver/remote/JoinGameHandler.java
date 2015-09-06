package jhangmanserver.remote;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import jhangmanserver.game_data.AbortedGameEvent;
import jhangmanserver.game_data.GameFullException;
import jhangmanserver.game_data.GameListHandler;
import jhangmanserver.game_data.GameStartingEvent;
import jhangmanserver.game_data.PlayerLeftEvent;
import tcp_interface.answers.Answer;
import tcp_interface.answers.JoinGameAnswer;
import tcp_interface.answers.JoinGameCompletedAnswer;
import tcp_interface.requests.JoinGameRequest;
import tcp_interface.requests.Request;
import utility.observer.JHObserver;
import utility.observer.ObservationHandler;

class JoinGameHandler extends TCPHandler {
    
    public JoinGameHandler(int id) {
        super(id);
    }

    public void handleJoinGame(
        JoinGameRequest request, 
        ObjectOutputStream outputStream, 
        ObjectInputStream inputStream,
        Socket socket,
        LoggedInChecker loggedInChecker,
        GameListHandler gameListHandler
    ) {
        try {
            printMessage("Starting to handle join game");
            String gameName = request.getGame();
            String nick = request.getNick();
            int cookie = request.getCookie();
            if (!loggedInChecker.isLoggedIn(nick, cookie)) {
                printMessage("User wasn't logged in");
                outputStream.writeObject(new JoinGameAnswer(false));
                return;
            } 
            if (!gameListHandler.isGameOpen(gameName)) {
                this.printMessage("Game wasn't open");
                outputStream.writeObject(new JoinGameAnswer(false));
                return;
            }
            
            JoinGameConfirmer confirmer = new JoinGameConfirmer(
                    nick,
                    outputStream,
                    inputStream,
                    socket
            );
            try {
                gameListHandler.joinGame(nick, gameName, confirmer);
            } catch (GameFullException e) {
                this.printMessage("Game was full!");
            } 
            outputStream.writeObject(new JoinGameAnswer(true));
            boolean confirmed = confirmer.handleConfirmation();
            if (!confirmed) {
                gameListHandler.leaveGame(nick, gameName);
            }

        } catch (IOException e) {
            
        }
        
    }
    
    private class JoinGameConfirmer implements JHObserver {

        private ObjectOutputStream outputStream;
        private ObjectInputStream inputStream;
        
        private Object eventLock = new Object();
        private EventID eventID = EventID.NOT_HANDLED;
        private String key;
        private InetAddress address;
        private Socket socket;
        private String nick;

        public JoinGameConfirmer(
                String nick,
                ObjectOutputStream outputStream, 
                ObjectInputStream inputStream,
                Socket socket
        ) {
            this.outputStream = outputStream;
            this.inputStream = inputStream;
            this.socket = socket;
            this.nick = nick;
        }

        public boolean handleConfirmation() {
            printMessage("Starting to handle join game confirmation");
            try {
                Request request = getRequest(this.inputStream);
                //received abort request, or protocol violated
                switch (request.getId()) {
                case ABORT:
                    printMessage("Received abort request");
                    return false;
                default:
                    printError("Received invalid request "+ 
                               "during join confirmation");
                    return false;
                }
              
            } catch (EOFException e) {
                //caught event or client close connection
                return this.handleEventOrClosed();
            } catch (IOException e) {
                printError("Got an IOException during join confirmation");
                return false;
            }
        }

        private boolean handleEventOrClosed() {
            synchronized(this.eventID) {
                switch (this.eventID) {
                case NOT_HANDLED:
                    printError("Client closed socket");
                    return false;
                case ABORT:
                    printMessage("Game was aborted");
                    try {
                        this.outputStream.writeObject(
                            new JoinGameCompletedAnswer(false, 
                                                        null, 
                                                        null)
                        );
                    } catch (IOException eIgnore) {
                    }
                    return false;
                case FULL:
                    printMessage("Game finally started!");
                    Answer answer = new JoinGameCompletedAnswer(
                        true, 
                        this.key, 
                        this.address);
                    try {
                        this.outputStream.writeObject(answer);
                        return true;
                    } catch (IOException e) {
                        return false;
                    }

                }
            }
            assert false;
            return false;
        }
        
        @ObservationHandler
        public void onGameStartingEvent(GameStartingEvent event) {
            synchronized(this.eventLock) {
                if (this.eventID == EventID.NOT_HANDLED) {
                    this.eventID = EventID.FULL;
                    this.key = event.getKey();
                    this.address = event.getAddress();
                }
            }
            try {
                this.socket.shutdownInput();
            } catch (IOException e) {
            }
        }
        
        @ObservationHandler
        public void onAbortedGameEvent(AbortedGameEvent event) {
            printMessage("Got aborted event");
            this.leaveGame();
        }
        
        private void leaveGame() {
            synchronized(this.eventLock) {
                printMessage("Current event status: " + this.eventID);
                if (this.eventID == EventID.NOT_HANDLED) {
                    this.eventID = EventID.ABORT;
                }
            }
            try {
                this.socket.shutdownInput();
            } catch (IOException e) {
                
            }
        }
        
        @ObservationHandler
        public void onPlayerLeftEvent(PlayerLeftEvent event) {
            printMessage("Got player left event");
            printMessage(String.format("Nicks? %s <-> %s", event.getNick(), this.nick));
            if (event.getNick().equals(this.nick)) {
                this.leaveGame();
            }
        }
        
        @Override
        public String toString() {
            return this.nick;
        }
    } 
}
