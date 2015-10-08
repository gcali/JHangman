package jhangmanclient.controller.player;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.LinkedList;
import java.util.Queue;

import jhangmanclient.controller.common.MessageSender;
import jhangmanclient.udp_interface.Message;
import jhangmanclient.udp_interface.master.GameUpdateMessage;
import jhangmanclient.udp_interface.player.GuessLetterMessage;
import jhangmanclient.udp_interface.player.GuessWordMessage;
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
 *  </ul>
 * @author gcali
 *
 */
public class PlayerController 
    implements Loggable, 
               Closeable, 
               JHObservable,
               JHObserver {

    private String nick;
    private InetAddress address;
    private String key;
    private MulticastSocket socket = null;
    private PlayerMessageDispatcher messageDispatcher;
    private PlayerMessageHandler messageHandler;
    
    private final JHObservableSupport observableSupport = 
        new JHObservableSupport();
    
    private MessageSender sender;

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
        this.messageHandler = new PlayerMessageHandler(messageQueue, messageLock);
        this.messageHandler.addObserver(this);
        this.messageHandler.start();
    }
    
    public void sendGuess(char letter) throws IOException {
        Message message = new GuessLetterMessage(letter, this.nick);
        this.sender.sendByteArrayToMulticast(message.encode(this.key)); 
    }
    
    public void sendGuess(String word) throws IOException {
        Message message = new GuessWordMessage(word, this.nick);
        this.sender.sendByteArrayToMulticast(message.encode(this.key)); 
    }

    @ObservationHandler
    public void onGameOverEvent(GameOverEvent e) {
        this.observableSupport.publish(e);
    }
    
    @ObservationHandler
    public void onUpdatedVisibleWordEvent(UpdatedPlayingStatusEvent e) {
        this.observableSupport.publish(e);
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
                    String.format("Update: %s (%d)", e.getWord(), e.getLives())
                );
            }
        });
        
        controller.start();
        
        controller.sendGuess('c');
        controller.sendGuess('i');
        controller.sendGuess('x');
        controller.sendGuess('p');
        controller.sendGuess('l');
        controller.sendGuess('l');
        controller.sendGuess('l');
        controller.sendGuess("ciao");
        System.out.println("[player] Guesses sent");
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        System.out.println("[player] Waited");
        controller.sendGuess('l');
        System.out.println("[player] Last guess sent");
        
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
}