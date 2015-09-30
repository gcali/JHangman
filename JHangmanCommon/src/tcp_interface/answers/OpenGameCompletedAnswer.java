package tcp_interface.answers;

import java.net.InetAddress;

public class OpenGameCompletedAnswer extends Answer {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public static final AnswerID id = AnswerID.OPEN_GAME_COMPLETED;
    private boolean accepted;
    private InetAddress address;
    private String key;
    private int port;
    
    public OpenGameCompletedAnswer(boolean accepted, 
                                   InetAddress address, 
                                   int port,
                                   String key) {
        super(id);
        this.accepted = accepted;
        this.address = address;
        this.port = port;
        this.key = key;
    }
    
    public static OpenGameCompletedAnswer createAborted() {
        return new OpenGameCompletedAnswer(false, null, -1, null);
    }
    
    public static OpenGameCompletedAnswer createAccepted(InetAddress address, 
                                                         int port,
                                                         String key) {
        return new OpenGameCompletedAnswer(true, address, port, key);
    }

    public synchronized boolean isAccepted() {
        return accepted;
    }

    public synchronized InetAddress getAddress() {
        return address;
    }

    public synchronized String getKey() {
        return key;
    } 
    
    public synchronized int getPort() {
        return this.port;
    }
}