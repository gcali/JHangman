package jhangmanclient.controller;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import udp_interface.Message;
import udp_interface.master.MasterHandshake;
import utility.Loggable;

public class GameMasterController implements Loggable, Closeable {
    
    private String nick;
    private InetAddress address;
    private int port;
    private String key;
    private String word = null;
    private MulticastSocket socket = null;
    private MessageDispatcher messageDispatcher;
    
    private Map<String, PlayerGameStatus> gameStatus = 
        new HashMap<String, PlayerGameStatus>();
    
    private Queue<Message> handshakeMessages = 
        new LinkedBlockingQueue<Message>();
    private Queue<Message> gameMessages =
        new LinkedBlockingQueue<Message>();
    private Thread handshakeMessagesHandler;
    private Thread gameMessagesHandler;

    public GameMasterController(String nick, 
                            InetAddress address, 
                            String key) {
        this.nick = nick;
        this.address = address;
        this.key = key;
        this.printDebugMessage("Address: " + address);
        this.printDebugMessage("Key: " + key);
    }

    public void setWord(String word) {
        this.word = word;
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
        Message handshake = new MasterHandshake(this.word);
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
        // TODO Auto-generated method stub
        
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
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (!shouldQuit) {
                try {
                    GameMasterController.this.socket.receive(packet);
                } catch (EOFException e) {
                    //socket closed, should quit
                    shouldQuit = true;
                } catch (IOException e) {
                    this.printError("Got an exception while receiving from" +
                                    " the socket, ignoring"); 
                }
            }
        }

        @Override
        public String getId() {
            return GameMasterController.this.getId() + " Dispatcher";
        } 
    } 
    
    class PlayerGameStatus {
        private final String nick;
        private int tries;
        private boolean winner;
        
        private Object lock = new Object();
        
        public PlayerGameStatus(String nick, int maxTries) {
            this.nick = nick;
            this.tries = maxTries;
            this.winner = false;
        }
        
        public boolean isWinner() {
            synchronized(this.lock) {
                return this.winner; 
            }
        }
        
        public void setWinner(boolean v) {
            synchronized(this.lock) {
                this.winner = v; 
            }
        }
        
        public int getTries() {
            synchronized(this.lock) {
                return this.tries;
            }
        }
        
        public void setTries(int v) {
            synchronized(this.lock) {
                this.tries = v;
            }
        }
        
        public int decrementTries() {
            synchronized(this.lock) {
                return --this.tries;
            }
        }
        
        public String getNick() {
            synchronized(this.lock) {
                return this.nick; 
            }
        }
    }
}