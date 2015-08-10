package rmi_interface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;

public interface ClientCallbackRMI extends Remote {

    /**
     * Add a new {@code game} to the game list with no players; 
     * if the game is already present, sets its players to 0
     * @param game the name of the game to be added
     * @throws RemoteException
     */
    public void addGame(String game) throws RemoteException;

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
     * Removes a {@code game} from the list, if present
     * @param game  the name of the game
     * @throws RemoteException
     */
    public void removeGame(String game) throws RemoteException;

    /**
     * Sets the current game data to reflect the given {@code gameList}, with
     * the associations (gameName -> numberOfPlayers)
     * @param gameList  a {@link Map} of associations
     * @throws RemoteException
     */
    public void setGameData(Map<String, Integer> gameList)
        throws RemoteException;
}