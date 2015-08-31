package jhangmanclient.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Callable;

import tcp_interface.answers.Answer;
import tcp_interface.requests.AbortRequest;
import tcp_interface.requests.OpenGameRequest;
import utility.observer.JHObserver;
import utility.observer.ObservationHandler;

class OpenGameTask implements Callable<MasterController>, JHObserver {
    
    private InetAddress address;
    private int port;
    private String nick;
    private int cookie;
    private Socket socket = null;

    public OpenGameTask(String nick, 
                        int cookie, 
                        InetAddress address, 
                        int port) {
        this.nick = nick;
        this.cookie = cookie;
        this.address = address;
        this.port = port; 
    }

    @Override
    public MasterController call() throws IOException {
        try (
                Socket socket = new Socket(this.address, this.port);
                ObjectOutputStream objOutput = 
                        new ObjectOutputStream(
                                new BufferedOutputStream(
                                    socket.getOutputStream()
                                )
                        );
                ObjectInputStream objInput =
                        new ObjectInputStream(
                                new BufferedInputStream(
                                        socket.getInputStream()
                                )
                        );
        ) {
            this.socket = socket;
            OpenGameRequest request = new OpenGameRequest(this.nick, 
                                                          this.cookie);
            objOutput.writeObject(request);
            Answer answer = null;
            try {
                answer = (Answer) objInput.readObject(); 
            } catch (EOFException e) {
                objOutput.writeObject(new AbortRequest());
                return null;
            }
            assert answer != null;
            
        } catch (IOException e) {
            throw e;
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
        return null;
    }
    
    @ObservationHandler
    public void onAbortTaskEvent(AbortTaskEvent event) {
        if (this.socket != null) {
            try {
                this.socket.shutdownInput();
            } catch (IOException e) { 
            }
        }
    } 
}