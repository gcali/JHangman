package jhangmanclient.controller;

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
import utility.JHObjectInputStream;
import utility.JHObjectOutputStream;
import utility.observer.JHObserver;
import utility.observer.ObservationHandler;

class OpenGameTask implements Callable<MasterController>, JHObserver {
    
    private InetAddress address;
    private int port;
    private String nick;
    private int cookie;
    private Socket socket = null;
    private int players;
    
    private static final int TIMEOUT = 10000;

    public OpenGameTask(String nick, 
                        int cookie, 
                        int players,
                        InetAddress address, 
                        int port) {
        this.nick = nick;
        this.cookie = cookie;
        this.address = address;
        this.port = port; 
        this.players = players;
    }
    
    private String getPrefixedMessage(String message) {
        return String.format("[%s] %s", this.nick, message);
    }
    
    private void printMessage(String message) {
        System.out.println(getPrefixedMessage(message));
    }
    
    private void printError(String message) {
        System.err.println(getPrefixedMessage(message));
    }

    @Override
    public MasterController call() throws IOException {
        printMessage("Hi, I'm starting!");
        try (
                Socket socket = new Socket(this.address, this.port);
                ObjectOutputStream objOutput =
                    new JHObjectOutputStream(socket.getOutputStream());
                ObjectInputStream objInput =
                    new JHObjectInputStream(socket.getInputStream());
        ) {
            printMessage("All streams open");
            this.socket = socket;
            OpenGameRequest request = new OpenGameRequest(this.nick, 
                                                          this.cookie,
                                                          this.players);
            printMessage("Writing request...");
            objOutput.writeObject(request);
            objOutput.flush();
            printMessage("Request written!");
            OpenGameAnswer firstAnswer = getOpenGameAnswer(objOutput, objInput);
            if (firstAnswer == null || !firstAnswer.isAccepted()) {
                return null;
            }
            OpenGameCompletedAnswer gameCompleteAnswer = 
                    getOpenGameCompleteAnswer(objOutput, objInput);
            if (gameCompleteAnswer == null || 
                !gameCompleteAnswer.isAccepted()) {
                return null;
            } 
            return new MasterController(this.nick,
                                        gameCompleteAnswer.getAddress(),
                                        gameCompleteAnswer.getPort(),
                                        gameCompleteAnswer.getKey());
        }
    }

    private static OpenGameCompletedAnswer getOpenGameCompleteAnswer(
            ObjectOutputStream objOutput, 
            ObjectInputStream objInput
    ) throws IOException {
        Answer answer = getAnswer(objOutput, objInput);
        if (answer == null) {
            return null;
        }
        switch (answer.getId()) {
        case OPEN_GAME_COMPLETED:
            return (OpenGameCompletedAnswer) answer;
        default:
            throw new IOException("Expected " + 
                                   OpenGameCompletedAnswer.id + 
                                   ", found " + answer.getId()); 
        }
    }

    private static OpenGameAnswer getOpenGameAnswer(
            ObjectOutputStream objOutput,
            ObjectInputStream objInput
    ) throws IOException {
        Answer answer = getAnswer(objOutput, objInput);
        if (answer == null) {
            return null;
        } 
        switch (answer.getId()) {
        case OPEN_GAME:
            return (OpenGameAnswer) answer;
        default:
            throw new IOException("Expected id " + OpenGameAnswer.id + 
                                  ", found " + answer.getId());
        }
    }

    private static Answer getAnswer(ObjectOutputStream objOutput,
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