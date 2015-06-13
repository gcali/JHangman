package game;

import java.net.InetAddress;

import development_support.NotImplementedException;

public class Game {
	
	private InetAddress address;
	private int port;
	private boolean over = false;

	public Game(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}
	
	public boolean guessLetter(char letter)
		throws IllegalStateException {
		throw new NotImplementedException();
	}
	
	public void leaveGame() 
		throws IllegalStateException {
		throw new NotImplementedException();
	}

	public boolean isOver() {
		return this.over;
	}

}