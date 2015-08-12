package tcp_interface.requests;

public class OpenGameParameters implements RequestParameters {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final String nick;
    private final int cookie;
    
    public OpenGameParameters(String nick, int cookie) {
        this.nick = nick;
        this.cookie = cookie;
    }
    
    public String getNick() {
        return this.nick;
    }
    
    public int getCookie() {
        return this.cookie;
    }
}