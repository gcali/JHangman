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
    
    public OpenGameCompletedAnswer(boolean accepted, 
                                   InetAddress address, 
                                   String key) {
        super(id);
        this.accepted = accepted;
        this.address = address;
        this.key = key;
    }
    
    public static OpenGameCompletedAnswer createAborted() {
        return new OpenGameCompletedAnswer(false, null, null);
    }
    
    public static OpenGameCompletedAnswer createAccepted(InetAddress address, 
                                                         String key) {
        return new OpenGameCompletedAnswer(true, address, key);
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
}