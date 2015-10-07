package jhangmanclient.controller.master;

import java.util.Queue;

import jhangmanclient.udp_interface.player.GuessMessage;
import utility.Loggable;
import utility.observer.JHObservable;
import utility.observer.JHObservableSupport;
import utility.observer.JHObserver;
import development_support.NotImplementedException;

/**
 * Published events:
 * <ul>
 *  <li>{@link InternalLetterGuessedEvent}</li>
 *  <li>{@link InternalWordGuessedEvent}</li>
 *  <li>{@link InternalWrongGuessEvent}</li>
 *  <li>{@link ConnectedPlayerEvent}</li>
 * </ul>
 * @author gcali
 */
public class GameMessagesHandler 
    extends Thread 
    implements Loggable, JHObservable {
    
    private volatile boolean gameOver = false;
    private Queue<GuessMessage> messageQueue;
    private Object lock;
    private String id = null;
    
    private JHObservableSupport observableSupport = new JHObservableSupport();
    private String wordToGuess;
    
    public GameMessagesHandler(Queue<GuessMessage> messageQueue,
                               Object gameMessagesLock,
                               String wordToGuess) {
        
        super();
        this.messageQueue = messageQueue;
        this.lock = gameMessagesLock; 
        this.wordToGuess = wordToGuess;
    }
    
    @Override
    public String getLoggableId() {
        return this.id != null ? this.id : "GameMessagesHandler";
    }
    
    public void setLoggableId(String id) {
        this.id = id;
    }
    
    @Override
    public void run() {
        boolean shouldQuit = false;
        GuessMessage message;
        while (!shouldQuit) {
            synchronized (this.lock) {
                message = this.messageQueue.poll();
                while (message == null && !this.gameOver) {
                    try {
                        this.lock.wait();
                    } catch (InterruptedException e) {
                    }
                    message = this.messageQueue.poll();
                }
                shouldQuit = this.gameOver;
            }
            if (message != null) {
                this.handleMessage(message);
            }
        }
    }

    private void handleMessage(GuessMessage message) {
        this.observableSupport.publish(
            new ConnectedPlayerEvent(message.getNick())
        );
        switch (message.getCategory()) {
        case GUESS_LETTER:
            String letter = message.getGuess();
            if (letter == null || letter.length() != 1) {
                printError("Invalid message received, discarding");
            } else {
                handleGuessLetter(letter.charAt(0)); 
            }
            break;
            
        case GUESS_WORD:
            String word = message.getGuess();
            if (word == null) {
                printError("Invalid message received, discarding");
            } else {
                handleGuessWord(word, message.getNick());
            }
            break;
            
        default:
            printError("Invalid message received, discarding");
            break;
        }
        
    } 

    private void handleGuessWord(String guess, String playerNick) {
        if (this.wordToGuess.equals(guess)) {
            this.observableSupport.publish(new InternalWordGuessedEvent(playerNick));
        } else {
            handleWrongGuess();
        }
    }


    private void handleGuessLetter(char guess) {
        boolean foundLetter = false;
        boolean[] guessed = new boolean[this.wordToGuess.length()];
        for (int i=0; i < this.wordToGuess.length(); i++) {
            if (this.wordToGuess.charAt(i) == guess) {
                guessed[i] = true;
                foundLetter = true;
            }
        }
        if (!foundLetter) {
            handleWrongGuess();
        } else {
            this.observableSupport.publish(new InternalLetterGuessedEvent(guessed));
        }
    }

    private void handleWrongGuess() {
        this.observableSupport.publish(new InternalWrongGuessEvent());
    }
    
    public void closeAndJoin() {
        throw new NotImplementedException(); 
    }

    @Override
    public void addObserver(JHObserver observer) {
        this.observableSupport.add(observer);
    }

    @Override
    public void removeObserver(JHObserver observer) {
        this.observableSupport.remove(observer);
    }

    
}
