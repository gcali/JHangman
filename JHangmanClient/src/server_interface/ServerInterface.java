package server_interface;

import game.Game;

import java.net.InetAddress;

import rmi_interface.UserAlreadyRegisteredException;
import rmi_interface.WrongPasswordException;
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
	
	public Game openGame(int players)
		throws IllegalStateException,
		       GameAlreadyExistsException,
			   ConnectionFailureException {
		throw new NotImplementedException();
	}
	
	public Game joinGame(String name)
		throws IllegalStateException,
			   GameNotFoundException,
			   ConnectionFailureException {
		return null;
		
	}
}