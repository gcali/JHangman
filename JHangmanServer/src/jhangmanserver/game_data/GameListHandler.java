package jhangmanserver.game_data;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import jhangmanserver.remote.rmi.CallbackProcedure;
import rmi_interface.ClientCallbackRMI;
import rmi_interface.SingleGameData;
import utility.Cleaner;
import utility.Loggable;
import utility.observer.JHObserver;

/**
 * Classe usata per mantenere la lista delle partite in apertura e
 * dei callback registrati dagli utenti;
 * @author gcali
 *
 */
public class GameListHandler implements Loggable {
    
    private Map<String, ServerGameData> gameDataMap = 
            new ConcurrentSkipListMap<String, ServerGameData>();
    private Map<String, ClientCallbackRMI> callbacks =
            new ConcurrentHashMap<String, ClientCallbackRMI>();
    
    private final Object openGamesLock = new Object();
    private int openGames = 0;
    private int maxOpenGames;
    
    /**
     * 
     * @param maxOpenGames numero massimo di partite che possono essere
     *                     in apertura in contemporanea
     */
    public GameListHandler(int maxOpenGames) {
        this.maxOpenGames = maxOpenGames;
    }
    
    /**
     * Aggiunge un nuovo callback alla lista
     * @param nick utente che ha richiesto la registrazione del callback
     * @param notifier callback
     */
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

    /**
     * Rimuove il callback registrato da un utente
     * @param nick
     */
    public void removeCallback(String nick) {
        this.callbacks.remove(nick); 
    }
    
    /**
     * Apre una nuova partita
     * @param name nome dell'utente che ha richiesto l'apertura
     * @param maxPlayers numero di giocatori richiesti dalla partita
     * @param observer osservatore da notificare; gli eventi sono quelli
     *                 di {@link ServerGameData}
     * @return un {@link Cleaner} per gestire la rimozione di osservatori
     *         
     * @throws FullListException in caso la partita non potesse essere aperta
     *                           perché la lista era piena
     */
    public Cleaner openGame(String name, int maxPlayers, JHObserver observer) 
        throws FullListException {
        System.out.println("Hi, I've been called!");
        if (maxPlayers <= 0) {
            throw new IllegalArgumentException(
                "Can't have non-positive players"
            );
        }
        ServerGameData data = new ServerGameData(name, maxPlayers);
        synchronized(openGamesLock) {
            if (openGames >= maxOpenGames) {
                throw new FullListException("" + openGames);
            }
            openGames++;
        }
        this.gameDataMap.put(name, data);
        System.out.println("Adding observer...");
        data.addObserver(observer);
        this.executeCallback(c -> c.addGame(name, maxPlayers));
        return new Cleaner() {
            
            @Override
            public void cleanUp() {
                data.removeObserver(observer); 
                printDebugMessage("Cleanup done");
            }
        };
    }
    
    /**
     * Annulla una partita
     * 
     * @param name nome della partita
     */
    public void cancelGame(String name) {
        synchronized(openGamesLock) {
            openGames--;
        }
        this.gameDataMap.remove(name);
        this.executeCallback(c -> c.removeGame(name));
    }
    
    /**
     * Verifica se una partita è aperta
     * 
     * @param name nome della partita
     * @return true se e solo se la partita era aperta
     */
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
     * @throws PlayerIsMasterException 
     */
    public Cleaner joinGame(String nick, 
                            String name, 
                            JHObserver fullGameObserver) 
                                throws GameFullException,
                                       PlayerAlreadyJoinedException, 
                                       PlayerIsMasterException {
        ServerGameData data = this.gameDataMap.get(name);
        if (data == null) {
            return Cleaner.newEmptyCleaner();
        }
        Cleaner cleaner = data.addPlayer(nick, fullGameObserver);
        //notifies the clients
        this.executeCallback(c -> c.incrementGamePlayers(name)); 
        return cleaner;
    }
    
    /**
     * Annulla la richiesta di partecipazione di un utente ad una partita
     * @param nick nome dell'utente
     * @param name nome della partita
     */
    public void leaveGame(String nick, String name) {
        ServerGameData data = this.gameDataMap.get(name);
        if (data == null) {
            return;
        }
        boolean removed = data.removePlayer(nick);
        if (removed) {
            //notifies the clients
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


    /**
     * Annulla tutte le partite legate in qualche modo all'utente
     * 
     * @param nick nome dell'utente
     */
    public void abortUserGames(String nick) {
        printDebugMessage("Abort for " + nick +"!");
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

    /**
     * Assegna ad una partita aperta la chiave di cifratura, l'indirizzo
     * del gruppo e la porta
     * @param gameName nome della partita
     * @param key chiave di cifratura
     * @param address indirizzo
     * @param port porta
     */
    public void setKeyAddressPort(
        String gameName, 
        String key, 
        InetAddress address,
        int port
    ) {
        ServerGameData data = this.gameDataMap.get(gameName);
        if (data != null) {
            data.setKeyAddressPort(key, address, port);
            this.cancelGame(gameName);
        }
    }

    @Override
    public String getLoggableId() {
        return "GameList";
    } 
}