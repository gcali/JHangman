package jhangmanclient.game_data;


public class NewGameEvent implements GameDataChangedEvent {
    
    private String name;
    private int maxPlayers;

    public NewGameEvent(String name, int maxPlayers) {
        this.name = name;
        this.maxPlayers = maxPlayers;
    }
    
    public String getName() {
        return this.name;
    }
    
    public int getMaxPlayers() {
        return this.maxPlayers;
    }

}
