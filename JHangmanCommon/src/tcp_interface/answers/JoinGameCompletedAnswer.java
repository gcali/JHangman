package tcp_interface.answers;

import java.net.InetAddress;

public class JoinGameCompletedAnswer extends Answer {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public static final AnswerID id = AnswerID.JOIN_GAME_COMPLETED;
    private final boolean accepted;
    private final String key;
    private final InetAddress address;
    private int port;
    
    public JoinGameCompletedAnswer(
        boolean accepted, 
        String key, 
        InetAddress address,
        int port
    ) {
        super(id); 
        this.accepted = accepted;
        this.key = key;
        this.address = address; 
        this.port = port;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String getKey() {
        return key;
    }

    public InetAddress getAddress() {
        return address;
    } 
    
    public int getPort() {
        return this.port;
    }
    
}