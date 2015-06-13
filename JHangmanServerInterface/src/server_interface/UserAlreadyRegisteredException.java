package server_interface;

public class UserAlreadyRegisteredException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1749185489790048708L;

	public UserAlreadyRegisteredException() {
		// TODO Auto-generated constructor stub
	}

	public UserAlreadyRegisteredException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public UserAlreadyRegisteredException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public UserAlreadyRegisteredException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public UserAlreadyRegisteredException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
