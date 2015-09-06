package jhangmanserver.game_data;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import jhangmanserver.remote.CallbackProcedure;
import rmi_interface.ClientCallbackRMI;
import rmi_interface.SingleGameData;
import utility.observer.JHObservable;
import utility.observer.JHObservableSupport;
import utility.observer.JHObserver;

public class GameListHandler implements JHObservable {
    
    private JHObservableSupport observableSupport = new JHObservableSupport();

    private Map<String, ServerGameData> gameDataMap = 
            new ConcurrentSkipListMap<String, ServerGameData>();
    private Map<String, ClientCallbackRMI> callbacks =
            new ConcurrentHashMap<String, ClientCallbackRMI>();
    
    public void addCallback(String nick, ClientCallbackRMI notifier) {
        this.callbacks.put(nick, notifier); 
        this.executeCallback(c -> c.setGameData(this.getGameList()));
    }

    private Map<String, SingleGameData> getGameList() {
        Map<String, SingleGameData> gameList = 
                new HashMap<String, SingleGameData>();
        for (Map.Entry<String, ServerGameData> e : this.gameDataMap.entrySet()) {
            ServerGameData data = e.getValue();
            gameList.put( 
                    e.getKey(), 
                    new SingleGameData(
                            data.getName(), 
                            data.getMaxPlayers(), 
                            data.getCurrentPlayers()
                    )
            ); 
        }
        return gameList;
    }

    public void removeCallback(String nick) {
        this.callbacks.remove(nick); 
    }
    
    public void openGame(String name, int maxPlayers, JHObserver observer) {
        System.out.println("Hi, I've been called!");
        ServerGameData data = new ServerGameData(name, maxPlayers);
        this.gameDataMap.put(name, data);
        System.out.println("Adding observer...");
        data.addObserver(observer);
        this.executeCallback(c -> c.addGame(name, maxPlayers));
    }
    
    public void cancelGame(String name) {
        this.gameDataMap.remove(name);
        this.executeCallback(c -> c.removeGame(name));
    }
    
    public boolean isGameOpen(String name) {
        if (this.gameDataMap.get(name) == null) {
            return false;
        } else {
            return true;
        }
    }
    
    
    /**
     * Updates data so that {@code nick} joins the game {@code name}.
     * If the game is consequently complete, returns the set of players
     * and removes the game; otherwise, returns {@code null}
     * @param nick the name of the player joining the game
     * @param name the name of the game
     * @throws GameFullException if the player could not join because
     *                           the game was full
     */
    public void joinGame(String nick, 
                         String name, 
                         JHObserver fullGameObserver) 
                                 throws GameFullException {
        ServerGameData data = this.gameDataMap.get(name);
        if (data == null) {
            return;
        }
        boolean complete = data.addPlayer(nick, fullGameObserver);
        this.executeCallback(c -> c.incrementGamePlayers(name)); 
        if (complete) {
            this.cancelGame(name);
        }
    }
    
    public void leaveGame(String nick, String name) {
        ServerGameData data = this.gameDataMap.get(name);
        if (data == null) {
            return;
        }
        boolean removed = data.removePlayer(nick);
        if (removed) {
            this.executeCallback(c -> c.decrementGamePlayers(name));
        }
    }
    
    private void executeCallback(CallbackProcedure procedure) {
        for (Entry<String, ClientCallbackRMI> entry : 
                this.callbacks.entrySet()) {
            try {
                procedure.execute(entry.getValue());
            } catch (RemoteException e) {
                System.err.println("Couldn't notify user " + entry.getKey());
            }
        }
    }

    @Override
    public void addObserver(JHObserver observer) {
        this.observableSupport.add(observer);
    }
    
    public void removeObserver(JHObserver observer) {
        this.observableSupport.remove(observer);
    }

    public void abortUserGames(String nick) {
        System.out.println("Abort!");
        ServerGameData data = this.gameDataMap.get(nick);
        if (data != null) {
            data.abortGame();
            this.cancelGame(nick);
        } 
        for (ServerGameData entry : this.gameDataMap.values()) {
            System.out.println("Removing partecipating games");
            if (entry.isPlayerIn(nick)) {
                System.out.println("Leaving " + entry.getName());
                this.leaveGame(nick, entry.getName());
            }
        }
    }

    public void setKeyAddress(String gameName, String key, InetAddress address) {
        ServerGameData data = this.gameDataMap.get(gameName);
        if (data != null) {
            data.setKeyAddress(key, address);
        }
    }
    
    
}