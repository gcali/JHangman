package utility;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Cleaner implements Closeable {
    
    private final AtomicBoolean shouldClose = new AtomicBoolean(true);
    
    public abstract void cleanUp();
    
    @Override
    public final void close() throws IOException {
        if (this.shouldClose.compareAndSet(true, false)) {
            this.cleanUp(); 
        }
    } 
    
    public static Cleaner newEmptyCleaner() {
        return new Cleaner() {
            
            @Override
            public void cleanUp() {
                //do nothing
            }
        };
    }
}
