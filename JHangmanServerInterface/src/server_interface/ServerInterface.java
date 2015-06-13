package server_interface;

import java.net.InetAddress;

import development_support.NotImplementedException;


public class ServerInterface { 

	protected String nickname;

	protected ServerInterface(String nickname) {
		this.nickname = nickname;
	}

	public void register(String password)
		throws UserAlreadyRegisteredException, 
			   ConnectionFailureException {
		throw new NotImplementedException();
	}
	
	public void login(String password)
		throws WrongPasswordException,
			   ConnectionFailureException {
		throw new NotImplementedException();
	}
	
	public void logout()
			throws IllegalStateException,
				   ConnectionFailureException {
		throw new NotImplementedException();
	}
	
	public InetAddress openGame()
		throws IllegalStateException,
			   ConnectionFailureException {
		throw new NotImplementedException();
	}
}