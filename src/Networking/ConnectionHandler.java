package Networking;

import Core.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ConnectionHandler implements Runnable, ConnectionListener {

	private static final boolean DEBUG_ONCONNECT = true;
	private List<CommunicationHandler> clientList;
	private ObjectInputStream inFromClient;
	private ObjectOutputStream outToClient;
	private CommunicationHandler commHandler;

	public ConnectionHandler (Socket clientSocket, ServerMessageHandler msgHandler, List<CommunicationHandler> clientList) throws Exception {
		this.clientList = clientList;

		//Create streams
		try {
			this.outToClient = new ObjectOutputStream (clientSocket.getOutputStream ());
			this.outToClient.flush();
			this.inFromClient = new ObjectInputStream (clientSocket.getInputStream ());

			//Get client info here
			logONCONNECT ("Waiting for info...");
			Message msg = Message.cast(this.inFromClient.readObject());
			if (msg == null) {
				throw new Exception ("Invalid info");
			}
			logONCONNECT ("Info received!");
			
			//Create comm handler
			this.commHandler = new CommunicationHandler (msgHandler, msg.from, this.inFromClient, this.outToClient, this);
		}
		catch (IOException ex) {
			System.out.println ("ConnectionHandler: " + ex.getMessage ());
		}


	}

	@Override
	public void onConnect (CommunicationHandler commHandler) {
		//Add comm handler to list
		this.clientList.add (commHandler);
		System.out.println (commHandler.clientName + " connected");

		for (CommunicationHandler ch : clientList) {
			ch.sendData ("msg", "Welcome " + commHandler.clientName + "!");
		}
	}

	@Override
	public void onDisconnect (CommunicationHandler commHandler) {
		try {
			this.inFromClient.close ();
			this.outToClient.close ();
		}
		catch (Exception ex) {
			System.out.println ("onDisconnect: " + ex.getMessage ());
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
			System.out.println ("ConnectionHandler: " + ex.getMessage ());
		}
	}

	private void logONCONNECT (String msg) {
		if (DEBUG_ONCONNECT) {
			System.out.println (msg);
		}
	}
}
