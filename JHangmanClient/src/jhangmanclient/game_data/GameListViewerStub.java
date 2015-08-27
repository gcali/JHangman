package jhangmanclient.game_data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import rmi_interface.SingleGameData;
import utility.observer.JHObservableSupport;
import utility.observer.JHObserver;

public class GameListViewerStub implements GameListViewer, Runnable {
    
    private JHObservableSupport observableSupport = new JHObservableSupport();
    
    private List<SingleGameData> gameData = new ArrayList<SingleGameData>();
    private Random randomGenerator = new Random();
    private int counter = 0;
    
    @Override
    public void addObserver(JHObserver observer) {
        this.observableSupport.add(observer);
    }

    @Override
    public List<SingleGameData> getGameList() {
        List<SingleGameData> returnableGameData = new ArrayList<SingleGameData>();
        for (SingleGameData entry : this.gameData) {
            returnableGameData.add(new SingleGameData(entry));
        }
        return returnableGameData;
    }
    
    public void removeRandomGame() {
        int pos = randomGenerator.nextInt(this.gameData.size());
        SingleGameData data = this.gameData.remove(pos);
        System.out.println("[Viewer] Removed " + data.getName());
        this.observableSupport.publish(new RemovedGameEvent(data.getName()));
    }
    
    public void addRandomGame() {
        String name = "Game " + (counter++);
        int maxPlayers = this.randomGenerator.nextInt(5) + 1;
        this.gameData.add(new SingleGameData(name, maxPlayers));
        this.observableSupport.publish(new NewGameEvent(name, maxPlayers));
    }
    
    public void randomAction() {
        int pos = this.randomGenerator.nextInt(10);
        if (pos < this.gameData.size()) {
            if (this.randomGenerator.nextBoolean()) {
                this.removeRandomGame();
            } else {
                SingleGameData data = this.gameData.get(pos);
                if (data.getCurrentPlayers() == data.getMaxPlayers()) {
                    this.gameData.remove(pos);
                    this.observableSupport.publish(new RemovedGameEvent(data.getName()));
                } else {
                    this.gameData.set(
                            pos, 
                            new SingleGameData(
                                    data.getName(),
                                    data.getMaxPlayers(), 
                                    data.getCurrentPlayers()+1
                            )
                    );
                    this.observableSupport.publish(
                            new GamePlayersChangedEvent(data.getName())
                    );
                }
            }
        } else {
            this.addRandomGame();
        }
    }

    @Override
    public SingleGameData getSingleGameData(String game) throws NoGameException {
        for (SingleGameData entry : this.gameData) {
            if (entry.getName().equals(game)) {
                return new SingleGameData(entry);
            }
        }
        throw new RuntimeException("Couldn't find game " + game + " in stub viewer");
    }

    @Override
    public void run() {
        while (true) {
            this.randomAction();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        
    } 
}