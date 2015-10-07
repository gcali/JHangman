package jhangmanserver.remote.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;

import tcp_interface.requests.Request;


public abstract class TCPHandler {
    
    private int TIMEOUT = 300000;
    
    protected static Request getRequest(ObjectInputStream inputStream) 
            throws IOException {
        try {
            return (Request) inputStream.readObject();
        } catch (ClassNotFoundException e) {
            System.err.println("Internal protocol violated during " +
                               "TCP communication");
            throw new IOException(e);
        }
    } 
    
    protected int getTimeout() {
        return this.TIMEOUT;
    }


}
