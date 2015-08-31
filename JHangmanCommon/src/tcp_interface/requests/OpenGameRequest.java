package tcp_interface.requests;

public class OpenGameRequest extends Request {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public static final RequestID id = RequestID.OPEN_GAME;

    private String nick; 
    private int cookie;
    
    public OpenGameRequest(String nick, int cookie) {
        super(OpenGameRequest.id);
        this.nick = nick;
        this.cookie = cookie;
    }

    public synchronized String getNick() {
        return nick;
    }

    public synchronized int getCookie() {
        return cookie;
    }


}