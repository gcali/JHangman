package jhangmanserver.remote;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import development_support.NotImplementedException;
import rmi_interface.ClientCallbackRMI;

public class GameListHandler {
    
    private Map<String, List<String>> gameDataMap = 
            new ConcurrentSkipListMap<String, List<String>>();
    private Map<String, ClientCallbackRMI> callbacks =
            new ConcurrentHashMap<String, ClientCallbackRMI>();
    
    public void addCallback(String nick, ClientCallbackRMI notifier) {
        this.callbacks.put(nick, notifier); 
    }

    public void removeCallback(String nick) {
        this.callbacks.remove(nick); 
    }
    
    public void openGame(String name) {
        this.cancelGame(name);
    }
    
    public void cancelGame(String name) {
        this.gameDataMap.remove(name);
        for (Entry<String, ClientCallbackRMI> entry : 
                this.callbacks.entrySet()) {
            try {
                entry.getValue().removeGame(name);
            } catch (RemoteException e) {
                System.err.println("Couldn't notify user " + entry.getKey());
            }
        }
    }
    
    
    public void joinGame(String nick, String name) {
        
    }
    
    public void startGame(String nick, String name) {
        throw new NotImplementedException();
    }
    
}