package jhangmanclient.controller.player;

import java.io.IOException;
import java.util.UUID;

import jhangmanclient.controller.common.MessageSender;
import jhangmanclient.udp_interface.Message;
import utility.Loggable;
import utility.observer.JHObservable;
import utility.observer.JHObservableSupport;
import utility.observer.JHObserver;

/**
 * Published events:
 *  <ul>
 *      <li>{@link MessageLostEvent}</li>
 *  </ul>
 * @author gcali
 *
 */
public class PlayerMessageChecker extends Thread implements Loggable,
                                                            JHObservable {

    private MessageSender sender;
    private String key;
    private Message message;
    private boolean reset = false;
    private long timeout;
    
    private boolean shouldQuit = false;
    
    private final Object lock = new Object();
    
    private int maxTries = 5;
    
    private final JHObservableSupport observableSupport = 
        new JHObservableSupport();

    public PlayerMessageChecker(MessageSender sender,
                                String key) {
        this.sender = sender;
        this.key = key;
    }
    
    @Override
    public void run() {
        
        int tries = 0;
        
        while (!shouldQuit) {
            synchronized(lock) {
                while (message == null && !shouldQuit) {
                    tries = 0;
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                    }
                }
                reset = false;
            }
            if (shouldQuit) {
                break;
            }
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                
            }
            synchronized (lock) {
                if (!reset) {
                    try {
                        sender.sendByteArrayToMulticast(message.encode(key));
                    } catch (IOException e) {
                    }
                    tries += 1;
                } else {
                    tries = 0;
                }
            }
            if (tries > maxTries) {
                synchronized (lock) {
                    message = null;
                }
                tries = 0;
                observableSupport.publish(new MessageLostEvent());
            }
        } 
    }
    
    public void setMessageToBeAcked(Message message) {
        setMessageToBeAcked(message, 1000);
    }
    
    public void setMessageToBeAcked(Message message, long timeout) {
        printDebugMessage("Message to be acked: " + message);
        this.timeout = timeout;
        synchronized(lock) {
            if (message != null) {
                reset = true;
                this.interrupt();
            }
            this.message = message; 
            lock.notify();
        }
    }
    
    public void ackMessage(UUID uuid) {
        boolean acked = false;
        synchronized(lock) {
            printDebugMessage("" + message);
            if (message != null && uuid.equals(message.getUUID())) {
                message = null;
                reset = true;
                acked = true;
            }
        }
        if (acked) {
            observableSupport.publish(new AckedMessageEvent());
        }
            
    }
    
    public void abortAck() {
        synchronized(lock) {
            if (message != null) {
                reset = true;
                message = null;
                lock.notify();
            }
        }
    }
    
    public void close() {
        synchronized(lock) {
            message = null;
            reset = true;
            shouldQuit = true;
        }
    }
    

    @Override
    public String getLoggableId() {
        return "PlayerMessageChecker";
    }

    @Override
    public void addObserver(JHObserver observer) {
        observableSupport.add(observer);
    }

    @Override
    public void removeObserver(JHObserver observer) {
        observableSupport.remove(observer);
    }

}
