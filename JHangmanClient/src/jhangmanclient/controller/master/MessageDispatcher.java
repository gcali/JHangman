package jhangmanclient.controller.master;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Queue;

import udp_interface.Message;
import udp_interface.player.GuessMessage;
import udp_interface.player.PlayerConnectionMessage;
import utility.Loggable;

class MessageDispatcher extends Thread implements Loggable {
    
    private final Queue<PlayerConnectionMessage> connectionMessagesQueue;
    private final Object connectionMessagesLock;

    private final Queue<GuessMessage> gameMessages;
    private final Object gameMessagesLock;

    private final DatagramSocket socket;
    private final String key;
    
    private String id = null;

    public MessageDispatcher(
        Queue<PlayerConnectionMessage> connectionMessagesQueue,
        Object connectionMessagesLock, 
        Queue<GuessMessage> gameMessages,
        Object gameMessagesLock,
        DatagramSocket socket,
        String key
    ) {
        this.connectionMessagesQueue = connectionMessagesQueue;
        this.connectionMessagesLock = connectionMessagesLock;
        this.gameMessages = gameMessages;
        this.gameMessagesLock = gameMessagesLock;
        this.socket = socket;
        this.key = key;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[64 << 10];
        boolean shouldQuit = false;
        while (!shouldQuit) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                this.socket.receive(packet);
                byte[] data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), 
                                 packet.getOffset(), 
                                 data, 
                                 0, 
                                 packet.getLength());
                Message message = 
                    Message.decode(data, this.key);
                this.handleDecodedMessage(message);
            } catch (SocketException e) {
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
                synchronized(this.connectionMessagesLock) {
                    this.connectionMessagesQueue.add(
                        (PlayerConnectionMessage) message
                    ); 
                    this.connectionMessagesLock.notify();
                }
            }
            break;
            
        case GUESS:
            if (!(message instanceof GuessMessage)) {
                printError("Invalid message received; discarding");
            } else {
                synchronized(this.gameMessagesLock) {
                    this.gameMessages.add( 
                        (GuessMessage) message
                    ); 
                    this.gameMessagesLock.notify();
                }
            }
            break; 

        default:
            printError("Invalid message received; discarding");
            break; 
        }
        
    }

    @Override
    public String getLoggableId() {
        return this.id == null ? "Dispatcher" : this.id;
    } 
    
    public void setLoggableId(String id) {
        this.id = id;
    }

    public void closeAndJoin() {
        this.socket.close();
        while (this.isAlive()) {
            try {
                this.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } 
        }
    }
} 