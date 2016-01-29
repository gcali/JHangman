package jhangmanclient.controller.player;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import jhangmanclient.controller.common.MessageSender;
import jhangmanclient.udp_interface.Message;
import jhangmanclient.udp_interface.master.GameUpdateMessage;
import jhangmanclient.udp_interface.player.GuessLetterMessage;
import jhangmanclient.udp_interface.player.GuessWordMessage;
import jhangmanclient.udp_interface.player.PlayerConnectionMessage;
import jhangmanclient.udp_interface.player.PlayerConnectionMessage.Action;
import utility.Loggable;
import utility.observer.JHObservable;
import utility.observer.JHObservableSupport;
import utility.observer.JHObserver;
import utility.observer.ObservationHandler;

/**
 * Published events:
 *  <ul>
 *      <li>{@link UpdatedPlayingStatusEvent}</li>
 *      <li>{@link GameOverEvent}</li>
 *      <li>{@link AckEvent}</li>
 *      <li>{@link MessageLostEvent}</li>
 *  </ul>
 * @author gcali
 *
 */
public class PlayerController 
    implements Loggable, 
               Closeable, 
               JHObservable,
               JHObserver {

    private final String nick;
    private InetAddress address;
    private String key;
    private MulticastSocket socket = null;
    private PlayerMessageDispatcher messageDispatcher;
    private PlayerMessageHandler messageHandler;
    
    private final Object lock = new Object();
    
    private Message toBeAcked = null;
    
    private final JHObservableSupport observableSupport = 
        new JHObservableSupport();
    
    private MessageSender sender;
    private PlayerMessageChecker checker;
    
    public PlayerController(
        String nick, 
        String gameName, 
        InetAddress address,
        int port,
        String key
    ) throws IOException {

        this.nick = nick;
        this.address = address;
        this.key = key;
        this.socket = new MulticastSocket(port);
        this.socket.joinGroup(this.address);
        this.sender = new MessageSender(this.socket, address, port);
        this.checker = new PlayerMessageChecker(sender, key);
        this.printDebugMessage("Game name: " + gameName);
        this.printDebugMessage("Address: " + address);
        this.printDebugMessage("Key: " + key);
    }
    
    public void start() {
        Queue<GameUpdateMessage> messageQueue = 
            new LinkedList<GameUpdateMessage>();
        Object messageLock = new Object();
        this.messageDispatcher =
            new PlayerMessageDispatcher(
                messageQueue, 
                messageLock, 
                this.socket, 
                this.key
        ); 
        this.messageDispatcher.start();
        this.messageHandler = new PlayerMessageHandler(
            nick,
            messageQueue, 
            messageLock);
        this.messageHandler.addObserver(this);
        this.messageHandler.start();
        this.checker.addObserver(this);
        this.checker.start();
        try {
            sendHello();
        } catch (IOException e) {
            printError("Couldn't send hello");
        }
    }
    
    public void sendHello() throws IOException {
        printDebugMessage("Sending hello!");
        Message message = new PlayerConnectionMessage(nick, Action.HELLO, null);
        sender.sendByteArrayToMulticast(message.encode(key));
    }
    
    public void sendGuess(char letter) throws IOException {
        Message message = new GuessLetterMessage(
            letter, 
            this.nick, 
            UUID.randomUUID()
        );
        this.sender.sendByteArrayToMulticast(message.encode(this.key)); 
    }
    
    public void sendGuessToBeAcked(char letter) throws IOException,
                                                       GuessCollisionException {
        synchronized (lock) {
            if (toBeAcked != null) {
                throw new GuessCollisionException();
            } else {
                UUID uuid = UUID.randomUUID();
                Message message = new GuessLetterMessage (
                    letter,
                    this.nick,
                    uuid
                );
                toBeAcked = message;
                try {
                    printDebugMessage("Sending...");
                    this.sender.sendByteArrayToMulticast(message.encode(key));
                    checker.setMessageToBeAcked(message);
                    printDebugMessage("Sent!");
                } catch (IOException e) {
                    toBeAcked = null;
                    throw e;
                }
            }
        }
    }
    
    public void sendGuess(String word) throws IOException {
        Message message = new GuessWordMessage(
            word, 
            this.nick,
            UUID.randomUUID()
        );
        this.sender.sendByteArrayToMulticast(message.encode(this.key)); 
    }

    public void sendGuessToBeAcked(String word) throws IOException,
                                                       GuessCollisionException {
        synchronized(lock) {
            if (toBeAcked != null) {
                throw new GuessCollisionException();
            } else {
                UUID uuid = UUID.randomUUID();
                Message message = new GuessWordMessage(
                    word, 
                    this.nick,
                    uuid
                );
                toBeAcked = message;
                try {
                    this.sender.sendByteArrayToMulticast(message.encode(key)); 
                    checker.setMessageToBeAcked(message);
                } catch (IOException e) {
                    toBeAcked = null;
                    throw e;
}
            }
        }
    }
    
    
    public void abort() {
        Message message = 
            new PlayerConnectionMessage(
                this.nick, 
                Action.ABORT,
                UUID.randomUUID()
            );
        try {
            this.sender.sendByteArrayToMulticast(message.encode(this.key));
        } catch (IOException e) {
        } finally {
            this.close();
        }
    }

    @ObservationHandler
    public void onGameOverEvent(GameOverEvent e) {
        this.observableSupport.publish(e);
    }
    
    @ObservationHandler
    public void onUpdatedPlayingStatusEvent(UpdatedPlayingStatusEvent e) {
        this.observableSupport.publish(e);
    }

    @ObservationHandler
    public void onAckEvent(AckEvent event) {
        printDebugMessage("Got ack! " + event.getUUID());
        checker.ackMessage(event.getUUID());
    }
    
    @ObservationHandler
    public void onAckedMessageEvent(AckedMessageEvent event) {
        synchronized(lock) {
            toBeAcked = null;
        }
        observableSupport.publish(event);

    }


    @Override
    public String getLoggableId() {
        return "(P) " + this.nick;
    }

    @Override
    public void close() {
        if (this.socket != null) {
            try {
                this.socket.leaveGroup(this.address);
            } catch (IOException e) {
                this.printError("Couldn't leave multicast group, ignoring");
            }
            this.socket.close();
            this.socket = null;
            this.messageDispatcher.closeAndJoin();
            this.messageHandler.closeAndJoin();
        }
    }
    
    public static void main(String[] args) throws IOException {
        String nick = "gio";
        String gameName = "Master";
        String key = "ciao";
        String addressArg = "239.255.54.67";
        String portArg = "49312";
        String word = "ciao";
        
        
        try {
            int i = 0;
            nick = args[i++];
            key = args[i++];
            word = args[i++]; 
            gameName = args[i++];
            addressArg = args[i++];
            portArg = args[i++];
        } catch (ArrayIndexOutOfBoundsException e) { 
        }
        
        InetAddress address = InetAddress.getByName(addressArg);
        int port = Integer.parseInt(portArg);
        
        PlayerController controller = new PlayerController(
            nick, 
            gameName, 
            address, 
            port, 
        key);
        
        controller.addObserver(new JHObserver() {
            private final Loggable l = new Loggable() {
                
                @Override
                public String getLoggableId() {
                    return "player";
                }
            };
            @ObservationHandler
            public void onGameOverEvent(GameOverEvent e) {
                l.printDebugMessage("Game over");
            }
            
            @ObservationHandler
            public void onUpdatedPlayingStatusEvent(
                UpdatedPlayingStatusEvent e
            ) {
                l.printDebugMessage(
                    String.format("Update: %s (%d)", e.getWord(), e.getRemainingLives())
                );
            }
        });
        
        controller.start();
        
        controller.sendGuess('c');
        try {
            Thread.currentThread().sleep(100);
        } catch (InterruptedException e2) {
        }
        controller.sendGuess('i');
        try {
            Thread.currentThread().sleep(100);
        } catch (InterruptedException e2) {
        }
        controller.sendGuess('x');
        try {
            Thread.currentThread().sleep(100);
        } catch (InterruptedException e2) {
        }
        controller.sendGuess('p');
        try {
            Thread.currentThread().sleep(100);
        } catch (InterruptedException e2) {
        }
        controller.sendGuess('l');
        try {
            Thread.currentThread().sleep(100);
        } catch (InterruptedException e2) {
        }
        controller.sendGuess('l');
        try {
            Thread.currentThread().sleep(100);
        } catch (InterruptedException e2) {
        }
        controller.sendGuess('l');
        try {
            Thread.currentThread().sleep(100);
        } catch (InterruptedException e2) {
        }
        controller.sendGuess("ciao");
        try {
            Thread.currentThread().sleep(100);
        } catch (InterruptedException e2) {
        }
        System.out.println("[player] Guesses sent");
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        System.out.println("[player] Waited");
        controller.sendGuess('l');
        System.out.println("[player] Last guess sent");

        try {
            Thread.currentThread().sleep(5000);
        } catch (InterruptedException e2) {
        }
        
        controller.close();
        System.out.println("[player] Everything closed");
    }

    @Override
    public void addObserver(JHObserver observer) {
        this.observableSupport.add(observer);
    }

    @Override
    public void removeObserver(JHObserver observer) {
        this.observableSupport.remove(observer);
    } 
    
    @Override
    public boolean shouldPrintDebug() {
        return true;
    }

    public String getNick() {
        return nick;
    }
    
}