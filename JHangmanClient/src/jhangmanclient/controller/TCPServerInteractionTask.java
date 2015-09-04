package jhangmanclient.controller;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;

import tcp_interface.answers.Answer;
import tcp_interface.requests.AbortRequest;

public abstract class TCPServerInteractionTask<V> implements Callable<V> {

    private String nick;

    public TCPServerInteractionTask(String nick) {
        this.nick = nick;
    }

    protected static Answer getAnswer(
        ObjectOutputStream objOutput, 
        ObjectInputStream objInput
    ) throws IOException {
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
    
    private String getPrefixedMessage(String message) {
        return String.format("[%s] %s", this.nick, message);
    }

    protected void printMessage(String message) {
        System.out.println(getPrefixedMessage(message));
    }

    protected void printError(String message) {
        System.err.println(getPrefixedMessage(message));
    }

    public TCPServerInteractionTask() {
        super();
    }

}