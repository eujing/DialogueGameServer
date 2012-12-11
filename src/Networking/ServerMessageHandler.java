package Networking;

import Core.Message;
import Game.GameEngine;
import java.util.HashMap;
import java.util.List;

public class ServerMessageHandler {

	private List<CommunicationHandler> clientList;
	private GameEngine gEngine;
	public HashMap<String, Func> response;

	public ServerMessageHandler (List<CommunicationHandler> clientList, GameEngine gEngine) {
		this.clientList = clientList;
		this.gEngine = gEngine;
		response = new HashMap<> ();
		registerResponses ();
	}

	public interface Func {

		public void execute (Message data);
	}
	
	private void registerResponses () {
		response.put ("info", new Func () {
			@Override
			public void execute (Message msg) {
				System.out.println ("Registering " + msg.from);
				gEngine.registerPlayer((String) msg.data);
			}
		});
		
		response.put ("response", new Func () {
			@Override
			public void execute (Message msg) {
				for (CommunicationHandler ch : clientList) {
					ch.sendData (msg.tag, msg);
				}
			}
		});
	}
}
