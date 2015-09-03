package tcp_interface.answers;

public class JoinGameAnswer extends Answer {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public static final AnswerID id = AnswerID.JOIN_GAME;

    private boolean accepted;

    public JoinGameAnswer(boolean accepted) {
        super(id);
        this.accepted = accepted;
    } 
    
    public boolean isAccepted() {
        return this.accepted;
    }
    
}