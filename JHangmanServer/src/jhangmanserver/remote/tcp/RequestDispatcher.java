package jhangmanserver.remote.tcp;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketTimeoutException;
import java.util.Queue;

import tcp_interface.requests.Request;
import utility.Loggable;

class RequestDispatcher extends Thread implements Loggable {
    
    protected Queue<DispatchedRequest> queue;
    protected Object lock;
    private ObjectInputStream objectInputStream;
    private String id = "RequestDispatcher";

    public RequestDispatcher(
        Queue<DispatchedRequest> requestQueue, 
        Object requestLock,
        ObjectInputStream objectInputStream
    ) { 
        this.queue = requestQueue;
        this.lock = requestLock; 
        this.objectInputStream = objectInputStream;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public RequestDispatcher(ObjectInputStream objectInputStream) {
        this(null,null,objectInputStream);
        
    }
    @Override
    public void run() {
        boolean shouldQuit = false;
        while (!shouldQuit) {
            try {
                Request request = (Request) objectInputStream.readObject(); 
                this.handleRequest(DispatchedRequest.fromRequest(request));
            } catch (ClassNotFoundException e) {
                this.printError("Deserialization failed, ignoring");
            } catch (EOFException e) {
                this.printDebugMessage("Socket closed, getting out");
                this.signalEof();
                shouldQuit = true;
            } catch (SocketTimeoutException e) {
                this.printDebugMessage("Socket got a timeout");
                this.signalTimeout();
            } catch (IOException e) {
                this.printError("Got an IOException, getting out");
                shouldQuit = true;
            }
        }
    } 
    
    private void signalEof() {
        this.handleRequest(DispatchedRequest.eof());
    }
    
    private void signalTimeout() {
        this.handleRequest(DispatchedRequest.timeout());
    }

    public void handleRequest(DispatchedRequest request) {
        this.printDebugMessage("Handling request!");
        synchronized(this.lock) {
            try {
                this.queue.add(request); 
                this.lock.notify();
            } catch (IllegalStateException e) {
                this.printError("Couldn't add request, discarding it");
            } 
        }
    }

    @Override
    public String getLoggableId() {
        return this.id;
    }
    
    static class DispatchedRequest {
        
        private final boolean eof;
        private final boolean timeout;
        private final Request request;
        
        public DispatchedRequest(Request request) {
            this(false, false, request);
        }
        
        private DispatchedRequest(
            boolean eof, 
            boolean timeout, 
            Request request
        ) {
            this.eof = eof;
            this.timeout = timeout;
            this.request = request;
        } 
        
        public static DispatchedRequest fromRequest(Request request) {
            return new DispatchedRequest(request);
        }

        public static DispatchedRequest eof() {
            return new DispatchedRequest(true, false, null);
        }
        
        public static DispatchedRequest timeout() {
            return new DispatchedRequest(false, true, null);
        }
        
        public boolean isEof() {
            return this.eof;
        }
        
        public boolean isTimeout() {
            return this.timeout;
        }
        
        public boolean isRequest() {
            return this.request != null;
        } 
        
        public Request getRequest() {
            if (this.request == null) {
                throw new IllegalStateException("This isn't a request");
            }
            return this.request;
        }
    }
}