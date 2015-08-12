package tcp_interface.requests;

import java.io.Serializable;
import java.net.Socket;

import development_support.NotImplementedException;

public class Request implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final RequestID id;
    private final RequestParameters requestParameters;
    
    public Request(RequestID id, RequestParameters requestParameters) {
        this.id = id;
        this.requestParameters = requestParameters;
    }
    
    public RequestID getId() {
        return this.id;
    }
    
    public RequestParameters getParameters() {
        return this.requestParameters;
    }
    
    public void sendRequest(Socket socket) {
        throw new NotImplementedException();
    }

}