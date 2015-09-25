package jhangmanclient.controller.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import jhangmanclient.controller.player.PlayerController;
import tcp_interface.answers.Answer;
import tcp_interface.answers.JoinGameAnswer;
import tcp_interface.answers.JoinGameCompletedAnswer;
import tcp_interface.requests.JoinGameRequest;
import utility.JHObjectInputStream;
import utility.JHObjectOutputStream;
import utility.observer.JHObserver;

public class JoinGameTask extends TCPServerInteractionTask<PlayerController>
                          implements JHObserver {
    
    private Socket socket;
    private String gameName;
    private int cookie;
    private InetAddress address;
    private int port;
    private String nick;
    
    public JoinGameTask(String gameName,
                        String nick, 
                        int cookie, 
                        InetAddress address,
                        int port) {
        this.gameName = gameName;
        this.nick = nick;
        this.cookie = cookie;
        this.address = address;
        this.port = port;
    }

    @Override
    public PlayerController call() {
        try {
            Socket socket = new Socket(this.address, this.port);
            this.socket = socket;
        } catch (IOException e) {
            printError("Couldn't open connection with server");
        }
        try (
            ObjectOutputStream output = 
                new JHObjectOutputStream(this.socket.getOutputStream());
            ObjectInputStream input =
                new JHObjectInputStream(this.socket.getInputStream());
        ) { 
            printDebugMessage("Opened all streams");
            JoinGameRequest request = new JoinGameRequest(
                this.nick, 
                this.cookie, 
                this.gameName
            );
            printDebugMessage("Asking request");
            output.writeObject(request);
            printDebugMessage("Request sent");
            JoinGameAnswer joinStartAnswer = getJoinGameAnswer(output, input);
            if (joinStartAnswer == null || !joinStartAnswer.isAccepted()) {
                printDebugMessage("Got answer for join request, I didn't get through");
                return null;
            }
            printDebugMessage("Got answer, yuhu!"); 
            JoinGameCompletedAnswer joinCompletedAnswer =
                getJoinGameCompletedAnswer(output, input);
            printDebugMessage("Got complete answer");
            if (joinCompletedAnswer == null || !joinCompletedAnswer.isAccepted()) {
                printDebugMessage("And I wasn't accepted...");
                return null;
            }
            printDebugMessage("And I was accepted!");
            return new PlayerController(
                this.nick,
                this.gameName,
                joinCompletedAnswer.getAddress(),
                joinCompletedAnswer.getKey()
            );
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    private JoinGameCompletedAnswer getJoinGameCompletedAnswer(
        ObjectOutputStream output, 
        ObjectInputStream input
    ) throws IOException {
        printDebugMessage("Waiting for completed answer");
        Answer answer = getAnswer(output, input);
        printDebugMessage("Got answer");
        if (answer == null) {
            printDebugMessage("But was null");
            return null;
        }
        printDebugMessage("Parsing...");
        switch (answer.getId()) {
        case JOIN_GAME_COMPLETED:
            printDebugMessage("Right kind!");
            return (JoinGameCompletedAnswer) answer;
        default:
            printDebugMessage("Nope, wrong kind. Wonder why.");
            throw new IOException("Expected " + JoinGameCompletedAnswer.id + 
                                  ", got " + answer.getId());
        }
    }

    private JoinGameAnswer getJoinGameAnswer(
        ObjectOutputStream output,
        ObjectInputStream input
    ) throws IOException {
        Answer answer = getAnswer(output, input);
        if (answer == null) {
            return null;
        }
        switch (answer.getId()) {
        case JOIN_GAME:
            return (JoinGameAnswer) answer;
        default:
            throw new IOException("Expected " + JoinGameAnswer.id + 
                                  ", got " + answer.getId());
        }
    }

    @Override
    public String getLoggableId() {
        return this.nick;
    }

}
