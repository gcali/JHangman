package jhangmanclient.game;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import utility.observer.JHObservable;
import jhangmanclient.callback.NoGameException;

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