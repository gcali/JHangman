package rmi_interface;

public class WrongPasswordException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1308053763792697237L;

	public WrongPasswordException() {
		// TODO Auto-generated constructor stub
	}

	public WrongPasswordException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public WrongPasswordException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public WrongPasswordException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public WrongPasswordException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
