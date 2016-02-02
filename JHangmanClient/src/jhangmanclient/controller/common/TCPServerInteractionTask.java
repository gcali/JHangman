package jhangmanclient.controller.common;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;

import tcp_interface.answers.Answer;
import tcp_interface.requests.AbortRequest;
import utility.Loggable;

public abstract class TCPServerInteractionTask<V> 
    implements Loggable, Callable<V> {

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
    
}