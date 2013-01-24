package Game;

import Core.Logger;
import Networking.Server;

public class DialogueGameServer {

	public static void main (String[] args) {
		try {
			Server server = new Server ((short) 3000);
			server.startListening ();
		}
		catch (Exception ex) {
			Logger.logException ("DialogueGameServer::Main", ex);
		}
	}
}
