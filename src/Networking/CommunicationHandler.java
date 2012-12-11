package Networking;

import Core.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class CommunicationHandler {

	public String clientName;
	private ObjectInputStream inFromClient;
	private ObjectOutputStream outToClient;
	private ServerMessageHandler msgHandler;
	private ConnectionListener disconnectable;

	public CommunicationHandler (ServerMessageHandler msgHandler, String clientName, ObjectInputStream input, ObjectOutputStream output, ConnectionListener disconnectable) {
		this.clientName = clientName;
		this.inFromClient = input;
		this.outToClient = output;
		this.msgHandler = msgHandler;
		this.disconnectable = disconnectable;
	}

	public final void startListening () {
		Object obj = null;
		Message msg = null;

		try {
			while (true) {
				obj = this.inFromClient.readObject();
						
				//HANDLE messages here
				if ((msg = Message.cast(obj)) != null) {
					msgHandler.response.get (msg.tag).execute (msg);
				}
				else {
					System.out.println ("Invalid message from " + clientName);
				}
			}
		}
		catch (ClassNotFoundException classEx) {
			System.out.println (classEx.getMessage());
		}
		catch (IOException ioEx) {
			System.out.println (clientName + " disconnected");
		}
		finally {
			disconnect ();
		}
	}

	public final void sendData (String tag, Object data) {
		try {
			this.outToClient.writeObject(new Message (tag, this.clientName, data));
			this.outToClient.flush ();
		}
		catch (Exception ex) {
			System.out.println ("sendData: " + ex.getMessage ());
		}
	}

	public void disconnect () {
		Thread.currentThread ().interrupt ();
		this.disconnectable.onDisconnect (this);
	}
}
