package utility;


/**
 * Classe di utilità per facilitare la stampa a schermo di messaggi
 * di errore e di debugging; i metodi di default presenti nell'interfaccia
 * permettono di velocizzare l'implementazione delle funzioni di stampa,
 * e aiutano a disattivare le stampe in fase di produzione con facilità.
 * @author gcali
 *
 */
public interface Loggable {
    
    public String getLoggableId();
    
    static boolean printDebug = true;
    static boolean printError = true;

    default String getPrefixedMessage(String message) {
        return String.format("[%s] %s", this.getLoggableId(), message);
    }

    default void printDebugMessage(String message) {
        if (this.shouldPrintDebug()) {
            System.out.println(getPrefixedMessage(message)); 
        }
    }

    default void printError(String message) {
        if (this.shouldPrintError()) {
            System.err.println(getPrefixedMessage(message)); 
        }
    }
    
    default boolean shouldPrintDebug() {
        return printDebug;
    }
    
    default boolean shouldPrintError() {
        return printError;
    }
    
}