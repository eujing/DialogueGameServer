package Game;

import java.util.ArrayList;
import java.util.Collections;

public class ServerGameEngine {

	private final ArrayList<String> players;
	private int turnIndex;
	private boolean gameStarted;

	public ServerGameEngine () {
		this.players = new ArrayList<> ();
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

	public String getCurrentTurn () {
		return this.players.get (this.turnIndex);
	}

	public String getNextTurn () {
		this.turnIndex++;
		this.turnIndex %= players.size ();

		return this.getCurrentTurn ();
	}

	public void dropPlayer (String p) {
		synchronized (players) {
			players.remove (p);
		}
	}

	public void startGame () {
		this.turnIndex = 0;
		this.gameStarted = false;
		Collections.shuffle (this.players);
	}
}
