package rmi_interface;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface ClientCallbackRMI extends Remote, Serializable {

    /**
     * Add a new {@code game} to the game list with no current players and
     * {@code maxPlayers} max players; if the game is already present, 
     * sets its players to 0 and updates its max players
     * @param game the name of the game to be added
     * @param maxPlayers the number of players to wait for before starting
     *                   the game
     * @throws RemoteException
     */
    public void addGame(String game, int maxPlayers) throws RemoteException;

    /**
     * Sets the {@code number} of players of a {@code game} in the game list; 
     * if the game doesn't exist, adds a new game with the given {@code number}
     * of players
     * @param game      the name of the game
     * @param number    the number of players of the game
     * @throws RemoteException
     */
    public void setGamePlayers(String game, int number) throws RemoteException;

    /**
     * Increments the number of players of a {@code game} in the game list; 
     * if the game doesn't exist, does nothing
     * @param game      the name of the game
     * @throws RemoteException
     */
    public void incrementGamePlayers(String game) throws RemoteException;

    /**
     * Decrements the number of players of a {@code game} in the game list; 
     * if the game doesn't exist, does nothing
     * @param game      the name of the game
     * @throws RemoteException
     */
    public void decrementGamePlayers(String game) throws RemoteException;

    /**
     * Removes a {@code game} from the list, if present
     * @param game  the name of the game
     * @throws RemoteException
     */
    public void removeGame(String game) throws RemoteException;

    /**
     * Sets the current game data to reflect the given {@code gameList}, with
     * the associations (gameName -> gameData)
     * @param gameList  a {@link Map} of associations
     * @throws RemoteException
     */
    public void setGameData(Map<String, SingleGameData> gameList)
        throws RemoteException;
}