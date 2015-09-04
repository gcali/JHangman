package jhangmanclient.controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import tcp_interface.answers.Answer;
import tcp_interface.answers.JoinGameAnswer;
import tcp_interface.answers.JoinGameCompletedAnswer;
import tcp_interface.requests.JoinGameRequest;
import utility.JHObjectInputStream;
import utility.JHObjectOutputStream;
import utility.observer.JHObservableSupport;
import utility.observer.JHObserver;

public class JoinGameTask extends TCPServerInteractionTask<PlayerController>
                          implements JHObserver {
    
    private JHObservableSupport observableSupport = new JHObservableSupport();
    private Socket socket;
    private String gameName;
    private String nick;
    private int cookie;
    private InetAddress address;
    private int port;
    
    public JoinGameTask(String gameName,
                        String nick, 
                        int cookie, 
                        InetAddress address,
                        int port) {
        super(nick);
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
        } catch (IOException e) {
            printError("Couldn't open connection with server");
        }
        try (
            ObjectOutputStream output = 
                new JHObjectOutputStream(this.socket.getOutputStream());
            ObjectInputStream input =
                new JHObjectInputStream(this.socket.getInputStream());
        ) { 
            printMessage("Opened all streams");
            JoinGameRequest request = new JoinGameRequest(
                this.nick, 
                this.cookie, 
                this.gameName
            );
            printMessage("Asking request");
            output.writeObject(request);
            printMessage("Request sent");
            JoinGameAnswer joinStartAnswer = getJoinGameAnswer(output, input);
            if (joinStartAnswer == null || !joinStartAnswer.isAccepted()) {
                return null;
            }
            
            JoinGameCompletedAnswer joinCompletedAnswer =
                getJoinGameCompletedAnswer(output, input);
            if (!joinCompletedAnswer.isAccepted()) {
                return null;
            }
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
        Answer answer = getAnswer(output, input);
        if (answer == null) {
            return null;
        }
        switch (answer.getId()) {
        case JOIN_GAME_COMPLETED:
            return (JoinGameCompletedAnswer) answer;
        default:
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

}
