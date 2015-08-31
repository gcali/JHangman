package tcp_interface.requests;

public class AbortRequest extends Request {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public AbortRequest() {
        super(RequestID.ABORT);
    } 

}