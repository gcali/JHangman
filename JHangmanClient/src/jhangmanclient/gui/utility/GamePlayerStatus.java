package jhangmanclient.gui.utility;

public class GamePlayerStatus {
    
    private int currentPlayers;
    private int maxPlayers;

    public GamePlayerStatus(int currentPlayers, int maxPlayers) {
        this.currentPlayers = currentPlayers;
        this.maxPlayers = maxPlayers;
    }

    public synchronized int getCurrentPlayers() {
        return currentPlayers;
    }

    public synchronized int getMaxPlayers() {
        return maxPlayers;
    } 
}