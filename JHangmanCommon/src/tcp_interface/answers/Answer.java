package tcp_interface.answers;

import java.io.Serializable;

public class Answer implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final AnswerID id;
    
    public Answer(AnswerID id) {
        this.id = id;
    }
    
    public AnswerID getId() {
        return this.id;
    }
    
}