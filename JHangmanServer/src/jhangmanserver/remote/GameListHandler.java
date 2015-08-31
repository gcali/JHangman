package jhangmanserver.remote;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import jhangmanserver.game_data.ServerGameData;
import rmi_interface.ClientCallbackRMI;

public class GameListHandler {
    
    private Map<String, ServerGameData> gameDataMap = 
            new ConcurrentSkipListMap<String, ServerGameData>();
    private Map<String, ClientCallbackRMI> callbacks =
            new ConcurrentHashMap<String, ClientCallbackRMI>();
    
    public void addCallback(String nick, ClientCallbackRMI notifier) {
        this.callbacks.put(nick, notifier); 
    }

    public void removeCallback(String nick) {
        this.callbacks.remove(nick); 
    }
    
    public void openGame(String name, int maxPlayers) {
        this.gameDataMap.put(name, new ServerGameData(name, maxPlayers));
        this.executeCallback(c -> c.addGame(name, maxPlayers));
    }
    
    public void cancelGame(String name) {
        this.gameDataMap.remove(name);
        this.executeCallback(c -> c.removeGame(name));
    }
    
    
    /**
     * Updates data so that {@code nick} joins the game {@code name}.
     * If the game is consequently complete, returns the set of players
     * and removes the game; otherwise, returns {@code null}
     * @param nick the name of the player joining the game
     * @param name the name of the game
     * @return the set of players if the game is complete, {@code null}
     *         otherwise
     */
    public Set<String> joinGame(String nick, String name) {
        ServerGameData data = this.gameDataMap.get(nick);
        if (data == null) {
            return null;
        }
        boolean complete = data.addPlayer(nick);
        if (!complete) {
            this.executeCallback(c -> c.incrementGamePlayers(name)); 
            return null;
        } else {
            Set<String> players = data.getPlayers();
            this.cancelGame(name);
            return players;
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
    
    
}