package jhangmanclient.controller.master;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import jhangmanclient.udp_interface.player.GuessLetterMessage;
import jhangmanclient.udp_interface.player.GuessMessage;
import jhangmanclient.udp_interface.player.GuessWordMessage;
import utility.Loggable;
import utility.observer.JHObservable;
import utility.observer.JHObservableSupport;
import utility.observer.JHObserver;

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
class GameMessagesHandler 
    extends Thread 
    implements Loggable, JHObservable {
    
    private volatile boolean gameOver = false;
    private Queue<GuessMessage> messageQueue;
    private Object lock;
    private String id = null;
    private final Map<String, Set<UUID>> receivedMessages = 
        new HashMap<String, Set<UUID>>();
    
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
    
    public boolean checkAndAddMessage(String nick, UUID uuid) {
        Set<UUID> messages;
        if (receivedMessages.containsKey(nick)) {
            messages = receivedMessages.get(nick);
        } else {
            messages = new HashSet<UUID>();
            receivedMessages.put(nick, messages);
        }
        printDebugMessage("Why the null? " + messages);
        return messages.add(uuid);
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
            if (message != null && !this.gameOver) {
                this.handleMessage(message);
            }
        }
    }

    private void handleMessage(GuessMessage message) {
        String nick = message.getNick();
        this.observableSupport.publish(
            new ConnectedPlayerEvent(nick)
        );
        UUID uuid = message.getUUID();
        if (uuid == null || checkAndAddMessage(nick, uuid)) {
            printDebugMessage("New message");
            if (message instanceof GuessLetterMessage) {
                GuessLetterMessage letterMessage = (GuessLetterMessage) message;
                char letter = letterMessage.getLetter();
                handleGuessLetter(letter, letterMessage.getNick()); 
            } else if (message instanceof GuessWordMessage) {
                GuessWordMessage wordMessage = (GuessWordMessage) message;
                String word = wordMessage.getWord();
                if (word == null) {
                    printError("Invalid message received, discarding");
                } else {
                    handleGuessWord(word, wordMessage.getNick());
                }
            } 
        } else {
            printDebugMessage("Message already received");
        }
        
        this.observableSupport.publish(new SendUpdateEvent(nick, uuid));
    } 

    private void handleGuessWord(String guess, String playerNick) {
        if (this.wordToGuess.equals(guess)) {
            this.observableSupport.publish(new InternalWordGuessedEvent(playerNick));
        } else {
            handleWrongGuess();
        }
    }


    private void handleGuessLetter(char guess, String playerNick) {
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
            this.observableSupport.publish(
                new InternalLetterGuessedEvent(guessed, playerNick)
            );
        }
    }

    private void handleWrongGuess() {
        this.observableSupport.publish(new InternalWrongGuessEvent());
    }
    
    public void closeAndJoin() {
        synchronized (this.lock) {
            this.gameOver = true;
            this.lock.notify();
        }
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
