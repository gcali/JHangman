package tcp_interface.requests;

public class JoinGameRequest extends Request {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public static final RequestID id = RequestID.JOIN_GAME;
    private String nick;
    private int cookie;
    private String game;
    
    public JoinGameRequest(String nick, int cookie, String game) {
        super(JoinGameRequest.id);
        this.nick = nick;
        this.cookie = cookie;
        this.game = game;
    }

    public synchronized String getNick() {
        return nick;
    }

    public synchronized int getCookie() {
        return cookie;
    }

    public synchronized String getGame() {
        return game;
    }

}
