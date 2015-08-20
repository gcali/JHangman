package jhangmanclient.game;

import java.util.List;
import java.util.Map;

import utility.observer.JHObservable;
import jhangmanclient.callback.NoGameException;

/**
 * Interface to view data about the open games and their players.
 * <p/>
 * It publishes the following events:
 * <ul>
 *  <li> {@link NewGameEvent} a new game has been opened </li>
 *  <li> {@link RemovedGameEvent} a game has been removed from the list </li>
 *  <li> {@link GameDataInvalidatedEvent} the entire game data should
 *                                    be refreshed </li>
 * </ul>
 * @author gcali
 *
 */
public interface GameListViewer extends JHObservable {

    /**
     * Returns a list view of the open games; the list is made of the tuples
     * (name,players)
     * @return  the list of the games
     */
    public List<Map.Entry<String, Integer>> getGameList();

    /**
     * Returns the number of players for the given {@code game}; if the
     * chosen game isn't open, throws {@link NoGameException}
     * 
     * @param game  the name of the game
     * @return      a tuple, of the form (game, players)
     * @throws NoGameException
     */
    public int getGamePlayers(String game) throws NoGameException;

}