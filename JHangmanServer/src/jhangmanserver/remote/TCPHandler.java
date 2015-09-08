package jhangmanserver.remote;

import java.io.IOException;
import java.io.ObjectInputStream;

import tcp_interface.requests.Request;


public abstract class TCPHandler {
    
    private int TIMEOUT = 300000;
    
    private Integer id;

    public TCPHandler(Integer id) {
        this.id = id;
    } 
    
    protected void printMessage(String message) {
        System.out.println(this.buildMessage(message));
    }
    
    protected void printError(String message) {
        System.out.println(this.buildMessage(message));
    }
    
    protected String buildMessage(String message) {
        if (this.id != null) {
            return String.format("[Server %3d] %s", this.id, message); 
        } else {
            return message;
        }
    }


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
    
    protected int getID() {
        return this.id;
    }
    
    protected int getTimeout() {
        return this.TIMEOUT;
    }


}
