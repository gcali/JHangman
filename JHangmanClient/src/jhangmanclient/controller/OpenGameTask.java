package jhangmanclient.controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import tcp_interface.answers.Answer;
import tcp_interface.answers.OpenGameAnswer;
import tcp_interface.answers.OpenGameCompletedAnswer;
import tcp_interface.requests.OpenGameRequest;
import utility.JHObjectInputStream;
import utility.JHObjectOutputStream;
import utility.observer.JHObserver;
import utility.observer.ObservationHandler;

class OpenGameTask extends TCPServerInteractionTask<MasterController> 
                   implements JHObserver {
    
    private InetAddress address;
    private int port;
    private int cookie;
    private Socket socket = null;
    private int players;
    private String nick;
    
//    private static final int TIMEOUT = 10000;

    public OpenGameTask(String nick, 
                        int cookie, 
                        int players,
                        InetAddress address, 
                        int port) {
        super(nick);
        this.nick = nick;
        this.cookie = cookie;
        this.address = address;
        this.port = port; 
        this.players = players;
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