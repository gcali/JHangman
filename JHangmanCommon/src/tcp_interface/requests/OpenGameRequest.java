package tcp_interface.requests;

public class OpenGameRequest extends Request {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public static final RequestID id = RequestID.OPEN_GAME;

    private String nick; 
    private int cookie;
    private int players;
    
    public OpenGameRequest(String nick, int cookie, int players) {
        super(OpenGameRequest.id);
        this.nick = nick;
        this.cookie = cookie;
        this.players = players;
    }

    public synchronized String getNick() {
        return nick;
    }

    public synchronized int getCookie() {
        return cookie;
    }
    
    public int getPlayers() {
        return this.players;
    }


}