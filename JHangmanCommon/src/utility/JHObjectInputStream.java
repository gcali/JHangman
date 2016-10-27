package utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * Wrapper attorno agli InputStream
 * 
 * @author gcali
 *
 */
public class JHObjectInputStream extends ObjectInputStream {

    public JHObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

}
