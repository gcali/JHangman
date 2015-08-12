package jhangmanserver.remote;

import java.net.Socket;

import development_support.NotImplementedException;


public class ServerTask implements Runnable {
    
    private Socket socket;

    public ServerTask(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        throw new NotImplementedException();

    }

}
