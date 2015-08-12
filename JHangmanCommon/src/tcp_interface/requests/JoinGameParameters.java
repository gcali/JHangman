package tcp_interface.requests;

public class JoinGameParameters implements RequestParameters {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final String nick;
    private final int cookie;
    private final String game;
    
    public JoinGameParameters(String nick, int cookie, String game) {
        this.nick = nick;
        this.cookie = cookie;
        this.game = game;
    }

    public String getNick() {
        return nick;
    }

    public int getCookie() {
        return cookie;
    }

    public String getGame() {
        return game;
    }

}