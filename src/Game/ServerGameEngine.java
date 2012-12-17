package Game;

import java.util.ArrayList;
import java.util.Collections;

public class ServerGameEngine {

	private final ArrayList<String> players;
	private String currentTurn;
	private boolean gameStarted;

	public ServerGameEngine () {
		this.players = new ArrayList<> ();
		this.currentTurn = null;
		this.gameStarted = false;
	}

	public boolean registerPlayer (String p) {
		if (gameStarted) {
			return false;
		}
		
		synchronized (players) {
			players.add (p);
		}
		
		return true;
	}
	
	public void dropPlayer (String p) {
		synchronized (players) {
			players.remove (p);
		}
	}
	
	public void startGame () {
		this.gameStarted = false;
		Collections.shuffle(this.players);
	} 
}
