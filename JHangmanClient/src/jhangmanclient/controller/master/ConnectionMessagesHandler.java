package jhangmanclient.controller.master;

import java.util.Queue;

import udp_interface.player.PlayerConnectionMessage;
import utility.observer.JHObservable;
import utility.observer.JHObservableSupport;
import utility.observer.JHObserver;

class ConnectionMessagesHandler extends Thread implements JHObservable {
    
    private volatile boolean gameOver = false;
    private Queue<PlayerConnectionMessage> queue;
    private Object lock;
    
    private final JHObservableSupport observableSupport = 
        new JHObservableSupport();
    
    public ConnectionMessagesHandler(
        Queue<PlayerConnectionMessage> connectionMessagesQueue,
        Object connectionMessagesLock
    ) {
        super();
        this.queue = connectionMessagesQueue;
        this.lock = connectionMessagesLock; 
    }
    
    @Override
    public void run() {
        PlayerConnectionMessage message;
        boolean shouldQuit = false;
        while (!shouldQuit) {
            synchronized (this.lock) {
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
                switch (message.getAction()) {
                    case ABORT:
                        this.handleAbort(message.getNick());
                        break; 
                    case HELLO:
                        this.handleHello(message.getNick());
                        break;
                }
            }
        }
    }

    private void handleHello(String playerNick) {
        this.observableSupport.publish(new ConnectedPlayerEvent(playerNick));
    }

    private void handleAbort(String playerNick) {
        this.observableSupport.publish(new DisconnectedPlayerEvent(playerNick));
    }
    
    public void endGame() {
        synchronized(this.lock) {
            this.gameOver = true;
            this.lock.notify();
        }
    } 
    
    public void closeAndJoin() {
        this.endGame();
        while (this.isAlive()) {
            try {
                this.join();
            } catch (InterruptedException e) {
            } 
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