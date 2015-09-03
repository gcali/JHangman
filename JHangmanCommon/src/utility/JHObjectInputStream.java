package utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class JHObjectInputStream extends ObjectInputStream {

    public JHObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

}
