package jhangmanclient.controller;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import udp_interface.Message;
import udp_interface.master.GameUpdateMessage;
import udp_interface.master.MasterHandshakeMessage;
import udp_interface.master.MasterHandshakeRequest;
import udp_interface.player.GuessMessage;
import udp_interface.player.PlayerHandshake;
import utility.Loggable;

public class GameMasterController implements Loggable, Closeable {
    
    private String nick;
    private InetAddress address;
    private int port;
    private String key;
    private String word = null;
    private String winner = null;
    private boolean gameOver = false;
    private boolean [] uncovered = null;
    private MulticastSocket socket = null;
    private MessageDispatcher messageDispatcher;
    
    private int updateCounter = 0;
    
    private Object gmcLock = new Object();
    
    private Set<String> playerSet = new HashSet<String>();

    private BlockingQueue<PlayerHandshake> handshakeMessages = 
        new LinkedBlockingQueue<PlayerHandshake>();
    private BlockingQueue<GuessMessage> gameMessages =
        new LinkedBlockingQueue<GuessMessage>();
    private Thread handshakeMessagesHandler;
    private Thread gameMessagesHandler;
    private int remainingTries;

    public GameMasterController(String nick, 
                            InetAddress address, 
                            String key,
                            int maxTries) {
        this.nick = nick;
        this.address = address;
        this.key = key;
        this.remainingTries = maxTries;
        this.printDebugMessage("Address: " + address);
        this.printDebugMessage("Key: " + key);
    }

    public void setWord(String word) {
        this.word = word;
        this.uncovered = new boolean[word.length()];
    }
    
    /**
     * Must be called after {@link #setWord(String)}
     * @throws IOException
     */
    public void initConnection() throws IOException {
        if (this.word == null ) {
            throw new NullPointerException("Word not set");
        }
        this.socket = new MulticastSocket(); 
        Message handshake = new MasterHandshakeMessage(this.word);
        byte [] encryptedHandhsake = handshake.encode(this.key);
        DatagramPacket packet = new DatagramPacket(
            encryptedHandhsake, 
            encryptedHandhsake.length, 
            this.address, 
            this.port
        );
        this.socket.send(packet); 
        this.messageDispatcher = new MessageDispatcher();
        this.handshakeMessagesHandler = new Thread(new Runnable() {
            
            @Override
            public void run() {
                handleHandshakeMessages();
                
            }
        });
        this.gameMessagesHandler = new Thread(new Runnable() {
            
            @Override
            public void run() {
                handleGameMessages();
                
            }
        });
        
    }
    
    protected void handleGameMessages() {
        while (!this.isGameOver()) {
            try {
                GuessMessage message = this.gameMessages.take();
                if (message != null) {
                    if (!this.playerSet.contains(message.getNick())) {
                        this.sendHandshakeRequest(message.getNick());
                    } else {
                        this.handleSingleGuessMessage(message); 
                    }
                }
            } catch (InterruptedException e) {
            }
        }
    }

    private void handleSingleGuessMessage(GuessMessage message) {
        switch (message.getCategory()) {
        case GUESS_LETTER:
            String letter = message.getGuess();
            if (letter == null || letter.length() != 1) {
                printError("Invalid message received, discarding");
            } else {
                handleGuessLetter(letter.charAt(0)); 
            }
            break;
            
        case GUESS_WORD:
            String word = message.getGuess();
            if (word == null) {
                printError("Invalid message received, discarding");
            } else {
                handleGuessWord(word, message.getNick());
            }
            break;
            
        default:
            printError("Invalid message received, discarding");
            break;
        }
    }

    private void sendHandshakeRequest(String player) {
        Message message = new MasterHandshakeRequest(player);
        byte [] encryptedMessage = message.encode(this.key);
        try {
            this.sendByteArrayToMulticast(encryptedMessage);
        } catch (IOException e) {
            printError("Couldn't send handshake request to " + player + ", ignoring error");
        }
    }
    
    private void sendByteArrayToMulticast(byte [] data) throws IOException {
        DatagramPacket packet = new DatagramPacket(
            data,
            data.length,
            this.address,
            this.port
        );
        this.socket.send(packet);
    }

    private void handleGuessWord(String guess, String playerNick) {
        if (this.winner == null && this.word.equals(guess)) {
            this.winner = playerNick;
            this.gameOver = true;
            for (int i=0; i < this.uncovered.length; i++) {
                this.uncovered[i] = true;
            }
        } else {
            handleWrongGuess();
        }
    }

    private void handleWrongGuess() {
        this.remainingTries--;
        if (this.remainingTries <= 0) {
            this.gameOver = true;
        }
    }

    private void handleGuessLetter(char guess) {
        boolean foundLetter = false;
        for (int i=0; i < this.word.length(); i++) {
            if (this.word.charAt(i) == guess && !this.uncovered[i]) {
                this.uncovered[i] = true;
                foundLetter = true;
            }
        }
        if (!foundLetter) {
            handleWrongGuess();
        }
        try {
            sendUpdate();
        } catch (IOException e) {
            printError("Couldn't send update, ignoring error");
        }
    }
    
    private boolean isGameOver() {
        return this.gameOver;
    }

    private void sendUpdate() throws IOException {
        String visibleWord = this.getVisibleWord();
        String winnerNick = this.winner;
        boolean isOver = this.isGameOver();
        GameUpdateMessage message = new GameUpdateMessage(
            this.updateCounter++, 
            visibleWord, 
            isOver, 
            winnerNick
        );
        byte[] encodedMessage = message.encode(this.key);
        DatagramPacket packet = new DatagramPacket(
            encodedMessage, 
            encodedMessage.length, 
            this.address, 
            this.port
        );
        this.socket.send(packet);
    }

    private String getVisibleWord() {
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<this.uncovered.length; i++) {
            if (this.uncovered[i]) {
                builder.append(this.word.charAt(i));
            } else {
                builder.append('_');
            }
        }
        return builder.toString();
    }

    protected void handleHandshakeMessages() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getId() {
        return "(M) " + this.nick;
    }

    /**
     * Must be called before object destruction
     * 
     */
    @Override
    public void close() {
        this.socket.close(); 
    }
    
    class MessageDispatcher implements Runnable, Loggable {
        
        private boolean shouldQuit = false;

        @Override
        public void run() {
            byte[] buffer = new byte[64 << 10];
            while (!shouldQuit) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    GameMasterController.this.socket.receive(packet);
                    byte[] data = new byte[packet.getLength()];
                    System.arraycopy(packet.getData(), 
                                     packet.getOffset(), 
                                     data, 
                                     0, 
                                     packet.getLength());
                    Message message = 
                        Message.decode(data, GameMasterController.this.key);
                    this.handleDecodedMessage(message);
                } catch (EOFException e) {
                    //socket closed, should quit
                    shouldQuit = true;
                } catch (IOException e) {
                    this.printError("Got an exception while receiving from" +
                                    " the socket, ignoring"); 
                }
            }
        }

        private void handleDecodedMessage(Message message) {
            switch (message.getID()) {
            case PLAYER_HANDSHAKE:
                if (! (message instanceof MasterHandshakeMessage)) {
                    printError("Invalid message received; discarding");
                } else {
                    GameMasterController.this.handshakeMessages.add(
                        (PlayerHandshake) message
                    ); 
                }
                break;
                
            case GUESS:
                if (!(message instanceof GuessMessage)) {
                    printError("Invalid message received; discarding");
                } else {
                    GameMasterController.this.gameMessages.add( 
                        (GuessMessage) message
                    ); 
                }
                break; 

            default:
                printError("Invalid message received; discarding");
                break; 
            }
            
        }

        @Override
        public String getId() {
            return GameMasterController.this.getId() + " Dispatcher";
        } 
    } 
    
}