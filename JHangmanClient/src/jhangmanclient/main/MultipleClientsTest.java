package jhangmanclient.main;

import java.rmi.RemoteException;

import jhangmanclient.controller.common.AuthController;
import jhangmanclient.gui.frames.AuthFrame;

public class MultipleClientsTest {
    
    public static void main(String[] args) throws RemoteException {
        String hostName = "localhost";
        try {
            hostName = args[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            
        }
        
        GUIMain.User[] users = {new GUIMain.User("Gio", "test"),
                                new GUIMain.User("Mike", "test"),
                                new GUIMain.User("Phil", "test")};
        AuthFrame[] frames = new AuthFrame[users.length];
        for (int i=0; i<users.length; i++) {
            AuthController controller = GUIMain.initConnection(hostName);
            frames[i] = 
                GUIMain.startLogged(
                    users[i].getNick(), 
                    users[i].getPass(), 
                    controller
                );
        }
    }

}
