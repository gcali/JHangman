package jhangmanclient.controller.player;

import java.net.DatagramSocket;
import java.util.Queue;

import jhangmanclient.controller.common.MessageDispatcher;
import jhangmanclient.udp_interface.Message;
import jhangmanclient.udp_interface.master.GameUpdateMessage;

public class PlayerMessageDispatcher extends MessageDispatcher {

    private Queue<GameUpdateMessage> updateMessagesQueue;
    private Object updateMessagesLock;
    private Object helloMessagesLock;

    public PlayerMessageDispatcher(
        Queue<GameUpdateMessage> updateMessagesQueue,
        Object updateMessagesLock,
        DatagramSocket socket, 
        String key
    ) {
        super(socket, key);
        this.updateMessagesQueue = updateMessagesQueue;
        this.updateMessagesLock = updateMessagesLock;
    }

    @Override
    protected void handleDecodedMessage(Message message) {
        switch (message.getID()) {
        case GAME_UPDATE_MESSAGE:
            synchronized (this.updateMessagesLock) {
                this.updateMessagesQueue.add((GameUpdateMessage) message);
                this.updateMessagesLock.notify();
            }
            printDebugMessage("Got a game update message!");
            break;
            
        default:
            printDebugMessage("Got a useless message, ignoring");
            break;
        }

    }

}
