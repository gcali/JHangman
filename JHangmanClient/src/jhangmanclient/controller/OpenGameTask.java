package jhangmanclient.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;

import tcp_interface.answers.Answer;
import tcp_interface.answers.OpenGameAnswer;
import tcp_interface.answers.OpenGameCompletedAnswer;
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
    
    private static final int TIMEOUT = 10000;

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
            socket.setSoTimeout(TIMEOUT);
            objOutput.writeObject(request);
            Answer answer = getAnswer(objOutput, objInput);
            if (answer == null) {
                return null;
            } 
            switch (answer.getId()) {
            case OPEN_GAME:
                return this.returnControllerFromOpeningCompleted(
                        (OpenGameAnswer) answer, 
                        objOutput, 
                        objInput
                );
            default:
                throw new IOException("Expected id " + OpenGameAnswer.id + 
                                      ", found " + answer.getId());
            }
            
        } catch (IOException e) {
            throw e;
        }
    }

    private Answer getAnswer(ObjectOutputStream objOutput,
            ObjectInputStream objInput) throws IOException {
        Answer answer = null;
        try {
            answer = (Answer) objInput.readObject(); 
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        } catch (EOFException | SocketTimeoutException e) {
            try {
                objOutput.writeObject(new AbortRequest());
            } catch (Exception eIgnore) {
            }
        }
        return answer;
    }
    
    private MasterController returnControllerFromOpeningCompleted(
            OpenGameAnswer answer,
            ObjectOutputStream objOutput, 
            ObjectInputStream objInput
    ) throws IOException {
        if (!answer.isAccepted()) {
            return null;
        }
        
        Answer complete = this.getAnswer(objOutput, objInput);
        if (complete == null ) {
            return null;
        }
        switch (complete.getId()) {
        case OPEN_GAME_COMPLETED:
            return returnControllerFromCompletionAnswer(
                    (OpenGameCompletedAnswer) complete
            );
        default:
                throw new IOException("Expected " + 
                                       OpenGameCompletedAnswer.id + 
                                       ", found " + complete.getId());
        }
    }

    private MasterController returnControllerFromCompletionAnswer(
            OpenGameCompletedAnswer complete) {
        return new MasterController(this.nick,
                                    complete.getAddress(),
                                    complete.getPort(),
                                    complete.getKey());
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