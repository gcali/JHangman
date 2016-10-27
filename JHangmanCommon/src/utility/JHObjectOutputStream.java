package utility;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Wrapper attorno agli OutputStream per forzare il flush
 * @author gcali
 *
 */
public class JHObjectOutputStream extends ObjectOutputStream {

    public JHObjectOutputStream(OutputStream out) throws IOException {
        super(out);
        this.flush();
    }

}
