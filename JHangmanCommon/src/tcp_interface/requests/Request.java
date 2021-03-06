package tcp_interface.requests;

import java.io.Serializable;

public class Request implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final RequestID id;
    
    public Request(RequestID id) {
        this.id = id;
    }
    
    public RequestID getId() {
        return this.id;
    }
    
}