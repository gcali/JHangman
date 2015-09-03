package utility;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class JHObjectOutputStream extends ObjectOutputStream {

    public JHObjectOutputStream(OutputStream out) throws IOException {
        super(out);
        this.flush();
    }

}
