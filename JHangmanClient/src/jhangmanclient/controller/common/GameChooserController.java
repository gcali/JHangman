package jhangmanclient.controller.common;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

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

    public GameChooserController(RMIServer server, 
                                 String nick, 
                                 int cookie, 
                                 GameListViewer viewer) {
        this.address = getAddress();
        this.port = getPort(); 
//        this.threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
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
    
    private static void temporary() {
        printError("", "Using temporary function");
    }
    
    private static InetAddress getAddress() {
        temporary();
        try {
            return tcp_interface.Defaults.getAddress();
        } catch (UnknownHostException e) {
            assert false;
            return null;
        }
    }
    
    private static int getPort() {
        temporary();
        return tcp_interface.Defaults.getPort();
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
                                this.port);
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