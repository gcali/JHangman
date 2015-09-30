package jhangmanclient.controller.master;

import java.net.DatagramSocket;
import java.util.Queue;

import jhangmanclient.controller.common.MessageDispatcher;
import udp_interface.Message;
import udp_interface.player.GuessMessage;
import udp_interface.player.PlayerConnectionMessage;

public class MasterMessageDispatcher extends MessageDispatcher {

    private final Queue<PlayerConnectionMessage> connectionMessagesQueue;
    private final Object connectionMessagesLock;

    private final Queue<GuessMessage> gameMessages;
    private final Object gameMessagesLock;


    public MasterMessageDispatcher(
        Queue<PlayerConnectionMessage> connectionMessagesQueue,
        Object connectionMessagesLock, 
        Queue<GuessMessage> gameMessages,
        Object gameMessagesLock,
        DatagramSocket socket,
        String key
    ) {
        super(socket, key);
        this.connectionMessagesQueue = connectionMessagesQueue;
        this.connectionMessagesLock = connectionMessagesLock;
        this.gameMessages = gameMessages;
        this.gameMessagesLock = gameMessagesLock;
    } 

    @Override
    protected void handleDecodedMessage(Message message) {
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
            printDebugMessage("Invalid message received; discarding");
            break; 
        }
        
    }

}
