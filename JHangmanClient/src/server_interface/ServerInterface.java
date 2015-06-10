package server_interface;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import game.Game;

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
	
	public Game openGame()
		throws IllegalStateException,
			   ConnectionFailureException {
		throw new NotImplementedException();
	}
}