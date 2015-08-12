package tcp_interface.requests;

public class OpenGameRequest extends Request {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public static final RequestID id = RequestID.OPEN_GAME;
    
    public OpenGameRequest(String nick, int cookie) {
        super(OpenGameRequest.id, new OpenGameParameters(nick, cookie));
    } 

}