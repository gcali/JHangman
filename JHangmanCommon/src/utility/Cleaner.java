package utility;

import java.io.Closeable;
import java.io.IOException;

public interface Cleaner extends Closeable {
    
    public void cleanUp();
    
    @Override
    default public void close() throws IOException {
        this.cleanUp();
    }

}
