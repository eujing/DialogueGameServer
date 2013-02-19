package Game;

import Core.Logger;
import Networking.Server;

public class DialogueGameServer {

	public static void main (String[] args) {
		try {
			short port = 23;
			if (args.length == 1 && args[0].startsWith ("-port")) {
				port = Short.parseShort (args[0].replace (" ", "").replace ("-port=", ""));
			}
			Server server = new Server ((short) 23);
			server.startListening ();
		}
		catch (Exception ex) {
			Logger.logException ("DialogueGameServer::Main", ex);
		}
	}
}
