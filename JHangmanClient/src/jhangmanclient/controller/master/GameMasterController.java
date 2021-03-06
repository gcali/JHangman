package jhangmanclient.controller.master;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import jhangmanclient.controller.common.MessageDispatcher;
import jhangmanclient.controller.common.MessageSender;
import jhangmanclient.udp_interface.master.GameUpdateMessage;
import jhangmanclient.udp_interface.master.GameUpdateMessage.GameStatus;
import jhangmanclient.udp_interface.player.GuessMessage;
import jhangmanclient.udp_interface.player.PlayerConnectionMessage;
import utility.Loggable;
import utility.observer.JHObservable;
import utility.observer.JHObservableSupport;
import utility.observer.JHObserver;
import utility.observer.ObservationHandler;

/**
 * Published events:
 * <ul>
 *  <li>{@link LetterGuessedEvent}</li>
 *  <li>{@link WordGuessedEvent}</li>
 *  <li>{@link WrongGuessEvent}</li>
 *  <li>{@link LostGameEvent}</li>
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
    private int remainingLives;
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
    private MessageSender sender;
    private int maxLives;
    private long gameTimeout;

    public GameMasterController(String nick, 
                            InetAddress address,
                            int port,
                            String key,
                            int lives,
                            long gameTimeout) throws IOException {
        this.nick = nick;
        this.address = address;
        this.port = port;
        this.key = key;
        this.maxLives = lives;
        this.remainingLives = lives;
        this.socket = new MulticastSocket(this.port); 
        this.socket.joinGroup(this.address);
        this.sender = new MessageSender(this.socket, address, port);
        this.gameTimeout = gameTimeout;
        this.printDebugMessage("Address: " + address);
        this.printDebugMessage("Key: " + key);
        this.printDebugMessage("Constructor done");
    }

    public void setWord(String word) {
        this.word = word;
        this.uncovered = new boolean[word.length()];
        this.printDebugMessage("Word set");
    }
    
    /**
     * Must be called after {@link #setWord(String)}
     * @throws IOException
     */
    public void start() throws IOException {
        if (this.word == null ) {
            throw new NullPointerException("Word not set");
        }

        Queue<PlayerConnectionMessage> connectionMessagesQueue = 
            new ConcurrentLinkedQueue<PlayerConnectionMessage>();
        Object connectionMessagesLock = new Object();
        this.printDebugMessage("Connection messages queue created");

        Queue<GuessMessage> gameMessages =
            new ConcurrentLinkedQueue<GuessMessage>();
        Object gameMessagesLock = new Object();
        this.printDebugMessage("Game messages queue created");

        this.messageDispatcher = new MasterMessageDispatcher(
            connectionMessagesQueue,
            connectionMessagesLock,
            gameMessages,
            gameMessagesLock,
            this.socket,
            this.key
        );
        
        this.printDebugMessage("Message dispatcher created");

        this.connectionMessagesHandler = new ConnectionMessagesHandler(
            connectionMessagesQueue, 
            connectionMessagesLock
        );
        this.printDebugMessage("Connection messages handler created");

        this.gameMessagesHandler = new GameMessagesHandler(
            gameMessages, 
            gameMessagesLock, 
            word
        ); 
        this.printDebugMessage("Game messages handler created");
        
        this.connectionMessagesHandler.addObserver(this);
        this.gameMessagesHandler.addObserver(this);
        
        this.printDebugMessage("Observation started");
        
        this.printDebugMessage("Starting threads...");
        this.messageDispatcher.start();
        this.connectionMessagesHandler.start();
        this.gameMessagesHandler.start();
        this.printDebugMessage("All subthreads started");
        sendUpdate(null, null);
    }
    
    private void sendUpdate(String ackNick, UUID uuid) throws IOException {
        printDebugMessage("Sending update");
        GameUpdateMessage message;
        message = createUpdateMessage(ackNick, uuid);
        printDebugMessage("Sending update " + message.toString());
        byte[] encodedMessage = message.encode(this.key);
        this.sender.sendByteArrayToMulticast(encodedMessage);
    }

    private GameUpdateMessage createUpdateMessage(
        String ackNick, 
        UUID uuid 
    ) {
        GameUpdateMessage message;
        synchronized (this.lock) {
            String visibleWord = this.getVisibleWord();
            int remainingLives = this.remainingLives;
            int maxLives = this.maxLives;
            String winnerNick = this.winner;
            GameStatus status;
            if (!gameOver) {
                status = GameStatus.PLAYING;
            } else if (winnerNick != null) {
                status = GameStatus.WON;
            } else {
                status = GameStatus.LOST;
            }
            int counter = this.updateCounter++;
            message = new GameUpdateMessage(
                counter,
                visibleWord, 
                remainingLives,
                maxLives,
                status,
                winnerNick,
                ackNick,
                uuid
            ); 
        }
        return message;
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
            if (!gameOver) {
                try {
                    sendAbortNotice();
                } catch (IOException e) {
                    printError("Couldn't send abort notice");
                }
            }
            try {
                this.socket.leaveGroup(this.address);
            } catch (IOException e) {
            }
            this.socket.close(); 
            if (this.gameMessagesHandler != null) {
                this.gameMessagesHandler.closeAndJoin(); 
            }
            if (this.connectionMessagesHandler != null) {
                this.connectionMessagesHandler.closeAndJoin(); 
            }
            if (this.messageDispatcher != null) {
                this.messageDispatcher.closeAndJoin(); 
            }
            this.socket = null;
        } 
    }
    
    private void sendAbortNotice() throws IOException {
        printDebugMessage("Sending abort notice");
        GameUpdateMessage message;
        synchronized(lock) {
            message = new GameUpdateMessage(
                updateCounter++, 
                null, 
                0, 
                0, 
                GameStatus.ABORTED , 
                null, 
                null, 
                null
            );
        }
        byte[] encodedMessage = message.encode(this.key);
        this.sender.sendByteArrayToMulticast(encodedMessage);
    }

    @ObservationHandler
    public void onConnectedPlayerEvent(ConnectedPlayerEvent e) {
        this.printDebugMessage("Player connected! " + e.getNick());
        boolean shouldUpdate = false;
        synchronized(this.lock) {
            shouldUpdate = this.playerSet.add(e.getNick());
        } 
        if (shouldUpdate) {
            try {
                this.sendUpdate(null, null);
            } catch (IOException e1) {
                this.printError("Couldn't send update, ignoring error");
            } 
        }
    }
    
    @ObservationHandler
    public void onDisconnectedPlayerEvent(DisconnectedPlayerEvent e) {
        this.printDebugMessage("Player disconnected! " + e.getNick());
        synchronized(this.lock) {
            this.playerSet.remove(e.getNick());
        }
    } 
    
    @ObservationHandler
    public void onLetterGuessedEvent(InternalLetterGuessedEvent e) {
        boolean allDiscovered = true;
        synchronized(this.lock) {
            if (!this.gameOver) {
                boolean [] guessed = e.getGuessed();
                for (int i =0; i < this.uncovered.length; i++) {
                    this.uncovered[i] = this.uncovered[i] || guessed[i];
                    allDiscovered = allDiscovered && this.uncovered[i];
                } 
                gameOver = allDiscovered;
            } else {
                printDebugMessage("Someone guessed a letter, but the game was " +
                           "already over");
                return;
            }
        }
        printDebugMessage(getVisibleWord());
        if (allDiscovered) {
            this.observableSupport.publish(
                new WordGuessedEvent(word, e.getNick())
            );
        } else {
            this.observableSupport.publish(
                new LetterGuessedEvent(this.getVisibleWord())
            ); 
        }
    }
    
    @ObservationHandler
    public void onWordGuessedEvent(InternalWordGuessedEvent e) {
        this.printDebugMessage("Word guessed! " + e.getWinnerNick());
        synchronized(this.lock) {
            if (!this.gameOver) {
                for (int i=0; i < this.uncovered.length; i++) {
                    this.uncovered[i] = true;
                }
                this.winner = e.getWinnerNick();
                this.gameOver = true;
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
        this.printDebugMessage("Wrong guess...");
        boolean gameLost = false;
        synchronized(this.lock) {
            this.remainingLives--;
            if (this.remainingLives == 0) {
                this.gameOver = true;
                this.winner = null;
                gameLost = true;
            }
        }
        if (gameLost) {
            printDebugMessage("Game was lost!");
            this.observableSupport.publish(
                new LostGameEvent()
            );
        } else {
            this.observableSupport.publish(
                new WrongGuessEvent()
            );
        }
    }
    
    @ObservationHandler
    public void onSendUpdateEvent(SendUpdateEvent event) {
        try {
            sendUpdate(event.getNick(), event.getUUID());
        } catch (IOException e) {
            printError("Couldn't send update");
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
    
    public static void main(String[] args) throws IOException {
        
        String key = "ciao";
        String addressArg = "239.255.54.67";
        String portArg = "49312";
        String word = "ciao";
        String nick = "Master"; 
        
        try {
            int i = 0;
            while (true) {
                key = args[i++];
                word = args[i++];
                nick = args[i++];
                addressArg = args[i++];
                portArg = args[i++];
            } 
        } catch (ArrayIndexOutOfBoundsException e) { 
        } 
        
        InetAddress address = InetAddress.getByName(addressArg);
        int port = Integer.parseInt(portArg);
        
        GameMasterController controller = 
            new GameMasterController(
                nick, 
                address, 
                port, 
                key, 
                5,
                500000
            );
        
        controller.setWord(word);
        controller.start();
        
        Object lock = new Object();
        AtomicBoolean b = new AtomicBoolean();
        
        JHObserver observer = (new JHObserver() {
            @ObservationHandler
            public void onLetterGuessedEvent(LetterGuessedEvent e) {
                print("Letter guessed " + e.getVisibileWord());
            }
            
            @ObservationHandler
            public void onWordGuessedEvent(WordGuessedEvent e) {
                print("Word guessed " + e.getWinnerNick());
                try {
                    controller.sendUpdate(null, null);
                } catch (IOException e1) {
                }
                b.set(true);
                synchronized(lock) {
                    lock.notify();
                }
            }
            
            @ObservationHandler
            public void onWrongGuessEvent(WrongGuessEvent e) {
                print("Wrong guess");
            }
            
            @ObservationHandler
            public void onLostGameEvent(LostGameEvent e) {
                print("Lost game");
                b.set(true);
                synchronized(lock) {
                    lock.notify();
                }
            }
            
            private void print(String m) {
                System.out.println("[main] " + m);
            } 
        });
        controller.addObserver(observer);

        System.out.println("Looping!");
        while (!b.get()) {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        
        System.out.println("Removing observer!");
        controller.removeObserver(observer);
        System.out.println("Closing everything...");
        controller.close();
        System.out.println("I'm out of here!");
    }

    public int getMaxLives() {
        return maxLives;
    }

    public long getGameTimeout() {
        return gameTimeout;
    }
}