package Game;

import Core.ResponseHandler.Response;
import java.util.ArrayList;

public class GameEngine {

	private final ArrayList<String> players;
	private String currentTurn;

	public GameEngine () {
		this.players = new ArrayList<> ();
		this.currentTurn = null;
	}

	public void registerPlayer (String p) {
		synchronized (players) {
			players.add (p);
		}
	}
	
	public void dropPlayer (String p) {
		synchronized (players) {
			players.remove (p);
		}
	}
	
	
	public String getPlayer (String name) {
		for (String p : this.players) {
			if (p.equals (name)) {
				return p;
			}
		}
		
		return null;
	}
	
	public void startGame (String seed, Response type) {
		//todo
	} 
}
