package utility;


public class Loggable {

    private String id;

    public Loggable(String id) {
        this.id = id;
    }

    private String getPrefixedMessage(String message) {
        return String.format("[%s] %s", this.id, message);
    }

    public void printMessage(String message) {
        System.out.println(getPrefixedMessage(message));
    }

    public void printError(String message) {
        System.err.println(getPrefixedMessage(message));
    }

}