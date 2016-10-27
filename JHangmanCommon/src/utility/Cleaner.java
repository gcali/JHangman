package utility;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Classe per modellare le situazioni in cui un metodo necessita di avere
 * alcune azioni eseguite al termine del suo funzionamento; la classe
 * serve soprattuto a permettere di facilitare la costruzione di una funzione
 * di chiusura che può essere chiamata più volte senza conseguenze negative
 * @author gcali
 *
 */
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
