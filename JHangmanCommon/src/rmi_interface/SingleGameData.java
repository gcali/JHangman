package rmi_interface;

import java.io.Serializable;

public class SingleGameData implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String name;
    private int maxPlayers;
    private int currentPlayers;

    public SingleGameData(String name, int maxPlayers, int currentPlayers) {
        this.name = name;
        this.maxPlayers = maxPlayers;
        this.currentPlayers = currentPlayers;
    }
    
    public SingleGameData(String name, int maxPlayers) {
        this(name, maxPlayers, 0);
    }

    public SingleGameData(SingleGameData entry) {
        this(entry.name, entry.maxPlayers, entry.currentPlayers);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getCurrentPlayers() {
        return currentPlayers;
    }

    public void setCurrentPlayers(int currentPlayers) {
        this.currentPlayers = currentPlayers;
    }
    
}
