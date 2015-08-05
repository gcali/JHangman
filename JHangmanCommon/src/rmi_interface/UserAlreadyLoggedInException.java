package rmi_interface;

public class UserAlreadyLoggedInException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public UserAlreadyLoggedInException() {
        // TODO Auto-generated constructor stub
    }

    public UserAlreadyLoggedInException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public UserAlreadyLoggedInException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    public UserAlreadyLoggedInException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public UserAlreadyLoggedInException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        // TODO Auto-generated constructor stub
    }

}
