package jhangmanclient.controller.master;

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
import udp_interface.master.MasterHelloMessage;
import udp_interface.player.GuessMessage;
import udp_interface.player.PlayerConnectionMessage;
import utility.Loggable;

public class GameMasterController implements Loggable, Closeable {
    
    private String nick;
    private InetAddress address;
    private int port;
    private String key;
    private String word = null;
    private String winner = null;
    private Boolean gameOver = false;
    private boolean [] uncovered = null;
    private MulticastSocket socket = null;
    private MessageDispatcher messageDispatcher;
    
    private int updateCounter = 0;
    
    private final Object gameOverLock = new Object();
    private final Object connectionMessagesLock = new Object();
    private final Object gameMessagesLock = new Object();
    
    private Set<String> playerSet = new HashSet<String>();

    private BlockingQueue<PlayerConnectionMessage> connectionMessages = 
        new LinkedBlockingQueue<PlayerConnectionMessage>();
    private BlockingQueue<GuessMessage> gameMessages =
        new LinkedBlockingQueue<GuessMessage>();
    private Thread connectionMessagesHandler;
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
        this.socket.joinGroup(this.address);
        this.sendHello(); 
        this.messageDispatcher = new MessageDispatcher();
        this.connectionMessagesHandler = new Thread(new Runnable() {
            
            @Override
            public void run() {
                handleConnectionMessages();
                
            }
        });
        this.gameMessagesHandler = new Thread(new Runnable() {
            
            @Override
            public void run() {
                handleGameMessages();
                
            }
        });
        
    }

    private void sendHello() throws IOException {
        Message hello = new MasterHelloMessage(this.word);
        byte [] encryptedHello = hello.encode(this.key);
        DatagramPacket packet = new DatagramPacket(
            encryptedHello, 
            encryptedHello.length, 
            this.address, 
            this.port
        );
        this.socket.send(packet);
    }
    
    protected void handleGameMessages() {
        while (!this.isGameOver()) {
            try {
                GuessMessage message = this.gameMessages.take();
                if (message != null) {
                    if (!this.playerSet.contains(message.getNick())) {
                        this.playerSet.add(message.getNick());
                    }
                    this.handleSingleGuessMessage(message); 
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

    protected void handleConnectionMessages() {
        PlayerConnectionMessage message;
        while (!this.isGameOver()) {
            synchronized (this.connectionMessagesLock) {
                message = this.connectionMessages.poll();
                while (message == null && !this.isGameOver()) {
                    try {
                        this.connectionMessagesLock.wait();
                    } catch (InterruptedException e) {
                    }
                    message = this.connectionMessages.poll();
                }
            } 
            if (message != null) {
                switch (message.getAction()) {
                    case ABORT:
                        this.handleAbort(message.getNick());
                        break; 
                    case HELLO:
                        this.handleHello(message.getNick());
                        break;
                }
            }
        }
    }

    private void handleHello(String playerNick) {
        this.playerSet.add(playerNick);
    }

    private void handleAbort(String playerNick) {
        this.playerSet.remove(playerNick);
        if (this.playerSet.size() == 0) {
            //TODO Handle concurrency
            this.gameOver = true;
        }
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
        if (this.socket != null) {
            try {
                this.socket.leaveGroup(this.address);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            this.socket.close(); 
        }
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
            
            case PLAYER_CONNECTION_MESSAGE:
                if (!(message instanceof PlayerConnectionMessage)) {
                    printError("Invalid message received; discarding");
                } else {
                    synchronized(GameMasterController.this.connectionMessagesLock) {
                        GameMasterController.this.connectionMessages.add(
                            (PlayerConnectionMessage) message
                        ); 
                        GameMasterController.this.connectionMessagesLock.notify();
                    }
                }
                break;
                
            case GUESS:
                if (!(message instanceof GuessMessage)) {
                    printError("Invalid message received; discarding");
                } else {
                    synchronized(GameMasterController.this.gameMessagesLock) {
                        GameMasterController.this.gameMessages.add( 
                            (GuessMessage) message
                        ); 
                        GameMasterController.this.gameMessagesLock.notify();
                    }
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