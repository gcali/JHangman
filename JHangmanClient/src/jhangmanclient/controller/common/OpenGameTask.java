package jhangmanclient.controller.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import jhangmanclient.controller.master.GameMasterController;
import tcp_interface.answers.Answer;
import tcp_interface.answers.OpenGameAnswer;
import tcp_interface.answers.OpenGameCompletedAnswer;
import tcp_interface.requests.OpenGameRequest;
import utility.JHObjectInputStream;
import utility.JHObjectOutputStream;
import utility.observer.JHObserver;
import utility.observer.ObservationHandler;

class OpenGameTask extends TCPServerInteractionTask<GameMasterController> 
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
        this.nick = nick;
        this.cookie = cookie;
        this.address = address;
        this.port = port; 
        this.players = players;
    }
    
    @Override
    public GameMasterController call() throws IOException {
        printDebugMessage("Hi, I'm starting!");
        try (
                Socket socket = new Socket(this.address, this.port);
                ObjectOutputStream objOutput =
                    new JHObjectOutputStream(socket.getOutputStream());
                ObjectInputStream objInput =
                    new JHObjectInputStream(socket.getInputStream());
        ) {
            printDebugMessage("All streams open");
            this.socket = socket;
            OpenGameRequest request = new OpenGameRequest(this.nick, 
                                                          this.cookie,
                                                          this.players);
            printDebugMessage("Writing request...");
            objOutput.writeObject(request);
            printDebugMessage("Request written!");
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
            return new GameMasterController(this.nick,
                                        gameCompleteAnswer.getAddress(),
                                        gameCompleteAnswer.getPort(),
                                        gameCompleteAnswer.getKey(),
                                        10);
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

    @Override
    public String getLoggableId() {
        return this.nick;
    } 
}