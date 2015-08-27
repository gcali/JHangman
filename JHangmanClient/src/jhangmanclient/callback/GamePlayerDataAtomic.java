package jhangmanclient.callback;


public class GamePlayerDataAtomic {
    
    private String name;
    private int maxPlayers;
    private int currentPlayers;
    
    public GamePlayerDataAtomic(String name, int maxPlayers, int currentPlayers) {
        this.name = name;
        this.maxPlayers = maxPlayers;
        this.currentPlayers = currentPlayers;
    }

    public GamePlayerDataAtomic(String name, int maxPlayers) {
        this(name, maxPlayers, 0);
    }
    
    public synchronized void incrementCurrentPlayers() {
        this.currentPlayers++;
    }

    public synchronized void decrementCurrentPlayers() {
        this.currentPlayers--;
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized void setName(String name) {
        this.name = name;
    }

    public synchronized int getMaxPlayers() {
        return maxPlayers;
    }

    public synchronized void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public synchronized int getCurrentPlayers() {
        return currentPlayers;
    }

    public synchronized void setCurrentPlayers(int currentPlayers) {
        this.currentPlayers = currentPlayers;
    }
    
}