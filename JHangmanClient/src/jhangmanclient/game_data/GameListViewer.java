package jhangmanclient.game_data;

import java.util.List;

import rmi_interface.SingleGameData;
import utility.observer.JHObservable;

/**
 * Interface to view data about the open games and their players.
 * <p/>
 * It publishes the following events:
 * <ul>
 *  <li> {@link NewGameEvent} a new game has been opened </li>
 *  <li> {@link RemovedGameEvent} a game has been removed from the list </li>
 *  <li> {@link GameDataInvalidatedEvent} the entire game data should
 *                                    be refreshed </li>
 *  <li> {@link GamePlayersChangedEvent} the number of players of a game
 *                                    has been changed </li>
 * </ul>
 * @author gcali
 *
 */
public interface GameListViewer extends JHObservable {

    /**
     * Returns a list view of the open games
     * @return  the list of the games
     */
    public List<SingleGameData> getGameList();

    /**
     * Returns data for the given {@code game}; if the
     * chosen game isn't open, throws {@link NoGameException}
     * 
     * @param game  the name of the game
     * @return      the data of the game
     * @throws NoGameException
     */
    public SingleGameData getSingleGameData(String game) throws NoGameException;
    
}