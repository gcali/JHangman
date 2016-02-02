package jhangmanclient.controller.player;

import java.util.Queue;

import jhangmanclient.udp_interface.master.GameUpdateMessage;
import utility.Loggable;
import utility.observer.JHEvent;
import utility.observer.JHObservable;
import utility.observer.JHObservableSupport;
import utility.observer.JHObserver;

/**
 * Published events:
 *  <ul>
 *      <li>{@link UpdatedPlayingStatusEvent}</li>
 *      <li>{@link GameLostEvent}</li>
 *      <li>{@link GameAbortedEvent}</li>
 *      <li>{@link GameWonEvent}</li>
 *      <li>{@link AckEvent}</li>
 *  </ul>
 * @author gcali
 *
 */
public class PlayerMessageHandler 
    extends Thread 
    implements JHObservable, Loggable {
    
    private final JHObservableSupport observableSupport = 
        new JHObservableSupport();
    
    private String id = "PlayerMessageHandler";

    private String nick;
    private Queue<GameUpdateMessage> queue; 
    private Object lock;
    
    private boolean gameOver = false;
    
    private Integer lastSequenceNumber = null;
    
    public PlayerMessageHandler(
        String nick,
        Queue<GameUpdateMessage> messageQueue, 
        Object messageLock
    ) {
        super();
        this.nick = nick;
        this.queue = messageQueue;
        this.lock = messageLock; 
    }
    
    @Override
    public void run() {
        boolean shouldQuit = false;
        
        while (!shouldQuit) {
            GameUpdateMessage message;
            synchronized(this.lock) {
                message = this.queue.poll();
                while (message == null && !this.gameOver) {
                    try {
                        this.lock.wait();
                    } catch (InterruptedException e) {
                    }
                    message = this.queue.poll();
                }
                shouldQuit = this.gameOver;
            }
            if (message != null) {
                this.handleMessage(message);
            }
        } 
    }

    private void handleMessage(GameUpdateMessage message) {
        this.printDebugMessage("Starting packet handling " + this.nick);
        if (this.nick.equals(message.getNick())) {
            printDebugMessage("Got ack");
            this.publish(new AckEvent(message.getUUID()));
        }
        if (lastSequenceNumber == null || 
            message.getSequenceNumber() > lastSequenceNumber) {
            printDebugMessage("Message was recent");
            lastSequenceNumber = message.getSequenceNumber();
            switch (message.getStatus()) {
            case WON:
                handleGameWon(message.getVisibleWord(),
                              message.getNick());
                break;
            
            case ABORTED:
                handleGameAborted();
                break;
            
            case LOST:
                handleGameLost(message.getVisibleWord());
                break;
            
            case PLAYING:
                this.publish( 
                    new UpdatedPlayingStatusEvent(message.getVisibleWord(), 
                                                  message.getRemainingLives(),
                                                  message.getMaxLives())
                );
                break;
            }
//            if (message.isOver()) {
//                this.handleGameOver(message.getVisibleWord(), 
//                                    message.getWinnerNick());
//            } else {
//                this.publish( 
//                    new UpdatedPlayingStatusEvent(message.getVisibleWord(), 
//                                                  message.getRemainingLives(),
//                                                  message.getMaxLives())
//                );
//            }
        }
    }
    
    private void handleGameLost(String visibleWord) {
        printDebugMessage("Game was lost .-.");
        synchronized(this.lock) {
            gameOver = true;
        }
        publish(new GameLostEvent(visibleWord));
    }

    private void handleGameAborted() {
        synchronized(this.lock) {
            gameOver = true;
        }
        publish(new GameAbortedEvent()); 
    }

    private void handleGameWon(String visibleWord, String winnerNick) {
        synchronized(this.lock) {
            gameOver = true;
        }
        publish(new GameWonEvent(visibleWord, winnerNick)); 
    }

    private void publish(JHEvent event) {
        this.observableSupport.publish(event);
    }

    @Deprecated
    private void handleGameOver(String visibleWord, String winnerNick) {
        synchronized(this.lock) {
            this.gameOver = true; 
        }
        this.publish(new GameOverEvent(visibleWord, winnerNick));
    }

    @Override
    public String getLoggableId() {
        return this.id;
    }

    @Override
    public void addObserver(JHObserver observer) {
        this.observableSupport.add(observer);
    }

    @Override
    public void removeObserver(JHObserver observer) {
        this.observableSupport.remove(observer);
    }
    
    public void closeAndJoin() {
        synchronized(this.lock) {
            this.gameOver = true;
            this.lock.notify();
        }
    }
}