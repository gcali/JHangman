package utility;


public interface Loggable {
    
    public String getId();

    default String getPrefixedMessage(String message) {
        return String.format("[%s] %s", this.getId(), message);
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
        return true;
    }
    
    default boolean shouldPrintError() {
        return true;
    }
    
}