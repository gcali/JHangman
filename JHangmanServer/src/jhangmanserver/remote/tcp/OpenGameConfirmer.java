package jhangmanserver.remote.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

import jhangmanserver.address.AddressRange;
import jhangmanserver.address.MulticastAddressGenerator;
import jhangmanserver.game_data.AbortedGameEvent;
import jhangmanserver.game_data.GameFullEvent;
import jhangmanserver.remote.tcp.OpenGameHandler.OpenGameData;
import jhangmanserver.remote.tcp.RequestDispatcher.DispatchedRequest;
import tcp_interface.answers.OpenGameCompletedAnswer;
import utility.Loggable;
import utility.observer.JHObserver;
import utility.observer.ObservationHandler;

class OpenGameConfirmer extends TCPHandler
    implements JHObserver, Loggable { 
    private int timeout = this.getTimeout();
    private String key;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private Socket socket;
    private String name;

    private EventID eventId = EventID.NOT_HANDLED;
    private final Object lock = new Object();
    private String loggableId;
    private Queue<DispatchedRequest> queue;
    private AddressRange addressRange;
    
    public OpenGameConfirmer(String name,
                             String key,
                             AddressRange addressRange,
                             ObjectOutputStream output,
                             ObjectInputStream input,
                             Socket socket) {
        this.name = name;
        this.key = key;
        this.output = output;
        this.input = input;
        this.socket = socket;
        this.addressRange = addressRange;
    }
    
    public OpenGameData handleConfirmation() {
        try {
            this.socket.setSoTimeout(this.timeout);
        } catch (SocketException e) {
            printError("Couldn't set socket timeout");
            return null;
        }
        this.queue = new LinkedList<DispatchedRequest>();
        RequestDispatcher dispatcher = 
            new RequestDispatcher(queue, lock, this.input);
        dispatcher.start();
        this.printDebugMessage("Starting handling confirmation");
        DispatchedRequest request = null;
        synchronized(lock) {
            request = queue.poll();
            if (this.eventId != eventId.NOT_HANDLED) {
                return this.handleEvent(this.eventId);
            }
            while (request == null) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                }
                request = queue.poll();
                if (this.eventId != eventId.NOT_HANDLED) {
                    return this.handleEvent(this.eventId);
                }
            }
        }
        if (request.isEof()) {
            if (this.eventId != eventId.NOT_HANDLED) {
                return this.handleEvent(this.eventId);
            }
            this.printDebugMessage("Client closed connection");
            return null;
        } else if (request.isTimeout()) {
            this.printDebugMessage("Got timeout");
            return handleTimeout();
        } else {
            switch (request.getRequest().getId()) {
            case ABORT:
                this.printDebugMessage("Request was abort");
                return null;
            default:
                this.printError("Protocol violated, "+ 
                                "invalid message received"); 
                return null;
            }
        }
    }
    
    private void sendAbort() {
        try {
            this.output.writeObject(
                    OpenGameCompletedAnswer.createAborted()
            );
            this.printDebugMessage("Abort answer sent");
        } catch (IOException e1) {
            printError("Couldn't send abort notification to " + this.name);
        } 
    }

    private OpenGameData handleTimeout() {
        this.printDebugMessage("Handling timeout");
        this.sendAbort();
        return null;
    }
    
    private OpenGameData handleEvent(EventID id) {
        switch (id) {
        case FULL:
            return handleFullEvent();
        case ABORT:
            this.printDebugMessage("Game aborted");
            this.sendAbort();
            return null;
        default:
            assert false;
            return null;
        
        }
    }

    private OpenGameData handleFullEvent() {
        this.printDebugMessage("Game full!");
        InetAddress address = 
            MulticastAddressGenerator.getAddress(addressRange);
        if (address == null) {
            System.out.println("Couldn't find a free address");
            return null;
        }
        int port = MulticastAddressGenerator.getRandomPort();
        OpenGameData gameData = new OpenGameData(address, port);
        this.printDebugMessage("Generated address: " + address);
        try {
            this.output.writeObject(
                    OpenGameCompletedAnswer.createAccepted(address, 
                                                           port,
                                                           this.key)
            );
            return gameData;
        } catch (IOException e) {
            MulticastAddressGenerator.freeAddress(address);
            return null;
        }
    }

    @ObservationHandler
    public void onGameFull(GameFullEvent event) {
        synchronized(this.lock) {
            if (this.eventId == EventID.NOT_HANDLED) {
                this.eventId = EventID.FULL;
            }
            
        }
        try {
            this.socket.shutdownInput();
        } catch (IOException e) {
        }
    } 
    
    @ObservationHandler
    public void onGameAborted(AbortedGameEvent event) {
        synchronized(this.eventId) {
            if (this.eventId == EventID.NOT_HANDLED) {
                this.eventId = EventID.ABORT;
                try {
                    this.socket.shutdownInput();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public String getLoggableId() {
        return this.loggableId == null ? "OpenGameConfirmer" : this.loggableId;
    } 
    
    public void setLoggableId(String id) {
        this.loggableId = id;
    }
    
}
