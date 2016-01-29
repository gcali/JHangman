package jhangmanclient.controller.common;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import jhangmanclient.udp_interface.Message;
import utility.Loggable;

public abstract class MessageDispatcher extends Thread implements Loggable {
    
    private final DatagramSocket socket;
    private final String key;
    
    private String id = null;
    
    public MessageDispatcher(DatagramSocket socket, String key) {
        this.socket = socket;
        this.key = key;
    }

    @Override
    public void run() {
        this.printDebugMessage("Message dispatcher started");
        byte[] buffer = new byte[64 << 10];
        boolean shouldQuit = false;
        while (!shouldQuit) {
            this.printDebugMessage("Waiting for packet...");
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                this.socket.receive(packet);
                this.printDebugMessage("Packet received!");
                byte[] data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), 
                                 packet.getOffset(), 
                                 data, 
                                 0, 
                                 packet.getLength());
                Message message = 
                    Message.decode(data, this.key);
                this.printDebugMessage("Packet decoded");
                this.handleDecodedMessage(message);
                this.printDebugMessage("Packet handled"); 
            } catch (SocketException e) {
                //socket closed, should quit
                shouldQuit = true;
            } catch (IOException e) {
                this.printError("Got an exception while receiving from" +
                                " the socket, ignoring"); 
            }
        }
    }
    
    protected abstract void handleDecodedMessage(Message message);


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