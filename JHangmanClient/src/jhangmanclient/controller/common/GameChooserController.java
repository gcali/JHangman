package jhangmanclient.controller.common;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import jhangmanclient.config.ClientConfigData;
import jhangmanclient.game_data.GameListViewer;
import rmi_interface.RMIServer;
import rmi_interface.UserNotLoggedInException;
import utility.ActionExecutor;
import utility.observer.JHObservable;
import utility.observer.JHObservableSupport;
import utility.observer.JHObserver;

public class GameChooserController implements JHObservable {

    private RMIServer server;
    private String nick;
    private int cookie;
    private int logoutAction;
    private GameListViewer gameListViewer;
//    private final ThreadPoolExecutor threadPool;
    private final InetAddress address;
    private final int port;
    
    private JHObservableSupport observableSupport = new JHObservableSupport();
    private long gameTimeout;
    private int lives;

    public GameChooserController(RMIServer server, 
                                 String nick, 
                                 int cookie, 
                                 GameListViewer viewer,
                                 ClientConfigData configData) {
        this.address = getAddress(configData);
        this.port = configData.getTcpPort();
        this.lives = configData.getLives();
        this.gameTimeout = configData.getGameTimeout();
        this.server = server;
        this.nick = nick;
        this.cookie = cookie;
        this.gameListViewer = viewer;
        this.logoutAction = ActionExecutor.getActionExecutor().addAction(
            new Runnable() { 
                @Override
                public void run() {
                    try {
                        GameChooserController.this.basicHandleLogout(); 
                    } catch (Exception e) {
                        
                    }
                }
            }
        );
    }
    
    private static void printError(String prefix, String message) {
        System.err.println("[" + prefix + "] " + message);
    }
    
    private static InetAddress getAddress(ClientConfigData configData) {
        try {
            return InetAddress.getByName(configData.getTcpAddress());
        } catch (UnknownHostException e) {
            try {
                return InetAddress.getByName(tcp_interface.Defaults.getAddress());
            } catch (UnknownHostException e1) {
                assert false;
                throw new RuntimeException(e);
            }
        }
    }
    
    private void basicHandleLogout() throws RemoteException {
        try {
            this.server.logOut(this.nick, this.cookie);
        } catch (RemoteException e) {
            throw e;
        } catch (UserNotLoggedInException e) {
            System.err.println("User not logged in; ignoring the error");
        }
        
    } 

    public void handleLogout() throws RemoteException {
        ActionExecutor.getActionExecutor().removeAction(this.logoutAction);
        basicHandleLogout();
    } 
    
    public OpenGameTask openGame(int maxPlayers) {
        return new OpenGameTask(this.nick, 
                                this.cookie, 
                                maxPlayers, 
                                this.address, 
                                this.port,
                                gameTimeout,
                                lives);
    }
    
    public JoinGameTask joinGame(String name) {
        return new JoinGameTask(name, 
                                this.nick, 
                                this.cookie, 
                                this.address, 
                                this.port);
    }
    
    public GameListViewer getViewer() {
        return this.gameListViewer;
    }

    @Override
    public void addObserver(JHObserver observer) {
        this.observableSupport.add(observer);
    }

    @Override
    public void removeObserver(JHObserver observer) {
        this.observableSupport.remove(observer);
    } 
}