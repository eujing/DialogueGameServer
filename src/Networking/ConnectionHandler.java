package Networking;

import Core.Logger;
import Core.Message;
import Core.MessageHandler;
import Game.GameEngine;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ConnectionHandler implements Runnable, ConnectionListener {

	private static final boolean DEBUG = true;
	private List<CommunicationHandler> clientList;
	private ObjectInputStream inFromClient;
	private ObjectOutputStream outToClient;
	private GameEngine gEngine;
	private MessageHandler msgHandler;
	private CommunicationHandler commHandler;

	public ConnectionHandler (Socket clientSocket, final GameEngine gEngine, MessageHandler msgHandler, List<CommunicationHandler> clientList) throws Exception {
		this.gEngine = gEngine;
		this.clientList = clientList;
		this.msgHandler = msgHandler;

		//Create streams
		try {
			this.outToClient = new ObjectOutputStream (clientSocket.getOutputStream ());
			this.outToClient.flush ();
			this.inFromClient = new ObjectInputStream (clientSocket.getInputStream ());

			/*this.msgHandler.registerMessageListener ("info", new MessageListener () {
			 @Override
			 public void messageReceived (Message msg) {
			 commHandler = createCommHandler (msg);
			 System.out.println ("Registering " + msg.from);
			 gEngine.registerPlayer ((String) msg.data);
			 }
			 });*/

			//Get client info here
			Logger.logDebug ("Waiting for info...");
			Message msg = Message.cast (this.inFromClient.readObject ());
			if (msg == null) {
				throw new Exception ("Invalid info");
			}
			Logger.logDebug ("Info received!");

			this.commHandler = createCommHandler (msg);
		}
		catch (IOException ex) {
			Logger.logDebug ("ConnectionHandler: " + ex.getMessage ());
		}
	}

	private CommunicationHandler createCommHandler (Message msg) {
		return new CommunicationHandler (this.msgHandler, msg.from, this.inFromClient, this.outToClient, this);
	}

	@Override
	public void onConnect (CommunicationHandler commHandler) {
		//Add comm handler to list
		Logger.log ("Registering " + commHandler.clientName + "...");
		this.gEngine.registerPlayer (commHandler.clientName);
		this.clientList.add (commHandler);
		Logger.log (commHandler.clientName + " has connected");

		for (CommunicationHandler ch : clientList) {
			//ch.sendData ("msg", "Welcome " + commHandler.clientName + "!");
		}
	}

	@Override
	public void onDisconnect (CommunicationHandler commHandler) {
		try {
			this.inFromClient.close ();
			this.outToClient.close ();
		}
		catch (Exception ex) {
			Logger.logDebug ("onDisconnect: " + ex.getMessage ());
		}
		this.clientList.remove (commHandler);
	}

	@Override
	public void run () {
		try {
			this.onConnect (this.commHandler);
			this.commHandler.startListening ();
		}
		catch (Exception ex) {
			Logger.logDebug ("ConnectionHandler: " + ex.getMessage ());
		}
	}

}
