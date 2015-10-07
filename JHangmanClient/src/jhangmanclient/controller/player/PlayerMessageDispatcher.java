package jhangmanclient.controller.player;

import java.net.DatagramSocket;
import java.util.Queue;

import jhangmanclient.controller.common.MessageDispatcher;
import jhangmanclient.udp_interface.Message;
import jhangmanclient.udp_interface.master.GameUpdateMessage;
import jhangmanclient.udp_interface.master.MasterHelloMessage;

public class PlayerMessageDispatcher extends MessageDispatcher {

    private Queue<GameUpdateMessage> updateMessagesQueue;
    private Object updateMessagesLock;
    private Queue<MasterHelloMessage> helloMessagesQueue;
    private Object helloMessagesLock;

    public PlayerMessageDispatcher(
        Queue<GameUpdateMessage> updateMessagesQueue,
        Object updateMessagesLock,
        Queue<MasterHelloMessage> helloMessagesQueue,
        Object helloMessagesLock,
        DatagramSocket socket, 
        String key
    ) {
        super(socket, key);
        this.updateMessagesQueue = updateMessagesQueue;
        this.updateMessagesLock = updateMessagesLock;
        this.helloMessagesQueue = helloMessagesQueue;
        this.helloMessagesLock = helloMessagesLock;
    }

    @Override
    protected void handleDecodedMessage(Message message) {
        switch (message.getID()) {
        case MASTER_HELLO:
            synchronized (this.helloMessagesLock) {
                this.helloMessagesQueue.add((MasterHelloMessage) message);
                this.helloMessagesLock.notify();
            }
            printDebugMessage("Got hello from master!");
            break;
            
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
