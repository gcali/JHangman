package jhangmanclient.controller.common;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MessageSender {
    
    private MulticastSocket socket;
    private InetAddress address;
    private int port;

    public MessageSender(
        MulticastSocket socket, 
        InetAddress address, 
        int port
    ) {
        this.socket = socket;
        this.address = address;
        this.port = port; 
    }
    
    public void sendByteArrayToMulticast(byte [] data) throws IOException {
        DatagramPacket packet = new DatagramPacket(
            data,
            data.length,
            this.address,
            this.port
        );
        this.socket.send(packet);
    }

}
