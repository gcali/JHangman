package tcp_interface.answers;

public class OpenGameAnswer extends Answer {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private boolean accepted;

    public OpenGameAnswer(boolean accepted) {
        super(AnswerID.OPEN_GAME);
        this.accepted = accepted;
    } 
    
    public boolean isAccepted() {
        return this.accepted;
    }
    
}