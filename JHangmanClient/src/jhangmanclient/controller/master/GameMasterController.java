package jhangmanclient.controller.master;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import udp_interface.Message;
import udp_interface.master.GameUpdateMessage;
import udp_interface.master.MasterHelloMessage;
import udp_interface.player.GuessMessage;
import udp_interface.player.PlayerConnectionMessage;
import utility.Loggable;
import utility.observer.JHObservable;
import utility.observer.JHObservableSupport;
import utility.observer.JHObserver;
import utility.observer.ObservationHandler;

/**
 * Published events:
 * <ul>
 *  <li>{@link InternalWordGuessedEvent}</li>
 * </ul>
 * @author gcali
 *
 */
public class GameMasterController 
    implements Loggable, Closeable, JHObserver, JHObservable {
    
    private final String nick;
    private final InetAddress address;
    private final int port;
    private final String key;

    private String word = null;
    private int remainingTries;
    private final Object lock = new Object();

    private String winner = null;
    private boolean gameOver;

    private boolean [] uncovered = null;
    private MulticastSocket socket = null;
    
    private int updateCounter = 0;
    
    private Set<String> playerSet = new HashSet<String>();
    
    
    private MessageDispatcher messageDispatcher;
    private ConnectionMessagesHandler connectionMessagesHandler;
    private GameMessagesHandler gameMessagesHandler;
    
    private final JHObservableSupport observableSupport =
        new JHObservableSupport();

    public GameMasterController(String nick, 
                            InetAddress address,
                            int port,
                            String key,
                            int maxTries) {
        this.nick = nick;
        this.address = address;
        this.port = port;
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

        Queue<PlayerConnectionMessage> connectionMessagesQueue = 
            new ConcurrentLinkedQueue<PlayerConnectionMessage>();
        Object connectionMessagesLock = new Object();

        Queue<GuessMessage> gameMessages =
            new ConcurrentLinkedQueue<GuessMessage>();
        Object gameMessagesLock = new Object();

        this.messageDispatcher = new MessageDispatcher(
            connectionMessagesQueue,
            connectionMessagesLock,
            gameMessages,
            gameMessagesLock,
            this.socket,
            this.key
        );

        this.connectionMessagesHandler = new ConnectionMessagesHandler(
            connectionMessagesQueue, 
            connectionMessagesLock
        );

        this.gameMessagesHandler = new GameMessagesHandler(
            gameMessages, 
            gameMessagesLock, 
            word
        ); 
        
        this.connectionMessagesHandler.addObserver(this);
        this.gameMessagesHandler.addObserver(this);
        
        this.messageDispatcher.start();
        this.connectionMessagesHandler.start();
        this.gameMessagesHandler.start();
        
    }
    
    private void sendHello() throws IOException {
        Message hello = new MasterHelloMessage(this.word);
        byte [] encryptedHello = hello.encode(this.key);
        this.sendByteArrayToMulticast(encryptedHello);
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

    private void sendUpdate() throws IOException {
        GameUpdateMessage message;
        synchronized (this.lock) {
            String visibleWord = this.getVisibleWord();
            String winnerNick = this.winner;
            boolean isOver = this.gameOver;
            int counter = this.updateCounter++;
            message = new GameUpdateMessage(
                counter,
                visibleWord, 
                isOver, 
                winnerNick
            ); 
        }
        byte[] encodedMessage = message.encode(this.key);
        this.sendByteArrayToMulticast(encodedMessage);
    }

    public String getVisibleWord() {
        StringBuilder builder = new StringBuilder();
        synchronized(this.lock) {
            for (int i=0; i<this.uncovered.length; i++) {
                if (this.uncovered[i]) {
                    builder.append(this.word.charAt(i));
                } else {
                    builder.append('_');
                }
            } 
        }
        return builder.toString();
    }

    @Override
    public String getLoggableId() {
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
            }
            this.socket.close(); 
            this.gameMessagesHandler.closeAndJoin();
            this.connectionMessagesHandler.closeAndJoin();
            this.messageDispatcher.closeAndJoin();
            this.socket = null;
        } 
    }
    
    @ObservationHandler
    public void onConnectedPlayerEvent(ConnectedPlayerEvent e) {
        synchronized(this.lock) {
            this.playerSet.add(e.getNick());
        } 
    }
    
    @ObservationHandler
    public void onDisconnectedPlayerEvent(DisconnectedPlayerEvent e) {
        synchronized(this.lock) {
            this.playerSet.remove(e.getNick());
        }
    } 
    
    @ObservationHandler
    public void onLetterGuessedEvent(InternalLetterGuessedEvent e) {
        synchronized(this.lock) {
            if (!this.gameOver) {
                boolean [] guessed = e.getGuessed();
                for (int i =0; i < this.uncovered.length; i++) {
                    this.uncovered[i] = this.uncovered[i] && guessed[i];
                } 
            } else {
                printDebugMessage("Someone guessed a letter, but the game was " +
                           "already over");
                return;
            }
        }
        this.observableSupport.publish(
            new LetterGuessedEvent(this.getVisibleWord())
        );
    }
    
    @ObservationHandler
    public void onWordGuessedEvent(InternalWordGuessedEvent e) {
        synchronized(this.lock) {
            if (!this.gameOver) {
                for (int i=0; i < this.uncovered.length; i++) {
                    this.uncovered[i] = true;
                }
                this.winner = e.getWinnerNick();
                this.gameOver = true;
                try {
                    this.sendUpdate();
                } catch (IOException e1) {
                    printError("Couldn't send update after game won;" +
                               " ignoring the error");
                } 
            } else {
                printDebugMessage(e.getWinnerNick() + " got the word, but " +
                                  "the game was already over");
                return;
            }
        }
        this.observableSupport.publish(
            new WordGuessedEvent(this.word, e.getWinnerNick())
        );
    }
    
    @ObservationHandler
    public void onInternalWrongGuessEvent(InternalWrongGuessEvent e) {
        boolean gameLost = false;
        synchronized(this.lock) {
            this.remainingTries--;
            if (this.remainingTries == 0) {
                this.gameOver = true;
                this.winner = null;
                gameLost = true;
            }
        }
        if (gameLost) {
            this.observableSupport.publish(
                new LostGameEvent()
            );
        } else {
            this.observableSupport.publish(
                new WrongGuessEvent()
            );
        }
    }

    @Override
    public void addObserver(JHObserver observer) {
        this.observableSupport.add(observer);
    }

    @Override
    public void removeObserver(JHObserver observer) {
        this.observableSupport.remove(observer);
    }
}