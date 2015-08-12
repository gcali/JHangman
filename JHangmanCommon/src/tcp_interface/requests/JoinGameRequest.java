package tcp_interface.requests;

public class JoinGameRequest extends Request {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public static final RequestID id = RequestID.JOIN_GAME;
    
    public JoinGameRequest(String nick, int cookie, String game) {
        super(JoinGameRequest.id, new JoinGameParameters(nick, cookie, game));
    }

}
