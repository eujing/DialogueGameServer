package Networking;

import Core.Logger;
import Core.Message;
import Core.MessageHandler;
import Core.MessageListener;
import Core.MessageTag;
import Game.ServerGameEngine;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

	private static final int N_THREADS = 10;
	private ServerSocket serverSocket;
	private ExecutorService threadPool;
	private Thread listenThread;
	private List<CommunicationHandler> clientList;
	private MessageHandler msgHandler;
	private ServerGameEngine gEngine;

	public Server (short port) throws IOException {
		this.serverSocket = new ServerSocket (port);
		this.clientList = Collections.synchronizedList (new ArrayList<CommunicationHandler> ());
		this.threadPool = Executors.newFixedThreadPool (N_THREADS);
		this.gEngine = new ServerGameEngine ();
		this.msgHandler = new MessageHandler ();
		registerSendingListeners (this.msgHandler);
		registerReceivingListeners (this.msgHandler);

		//Connection listener
		this.listenThread = createListenThread ();

		//Listen for commands
		createCmdThread ().start ();
	}

	public void startListening () {
		this.listenThread.start ();
	}

	private void registerSendingListeners (MessageHandler msgHandler) {
		MessageListener defaultSend = new MessageListener () {
			@Override
			public void messageReceived (Message msg) {
				for (CommunicationHandler ch : clientList) {
					ch.sendData (msg);
				}
			}
		};

		MessageListener nPlayerUpdate = new MessageListener () {
			@Override
			public void messageReceived (Message msg) {
				for (CommunicationHandler ch : clientList) {
					ch.sendData (MessageTag.NUMBER_PLAYERS, clientList.size ());
				}
			}
		};

		msgHandler.registerSendingMessageListener (MessageTag.PLAYER_JOIN, nPlayerUpdate);
		msgHandler.registerSendingMessageListener (MessageTag.PLAYER_DROP, nPlayerUpdate);
	}

	private void registerReceivingListeners (MessageHandler msgHandler) {
		MessageListener defaultReceive = new MessageListener () {
			@Override
			public void messageReceived (Message msg) {
				for (CommunicationHandler ch : clientList) {
					ch.sendData (msg);
				}
			}
		};

		msgHandler.registerReceiveMessageListener (MessageTag.STOP_GAME, defaultReceive);

		msgHandler.registerReceiveMessageListener (MessageTag.START_GAME, new MessageListener () {
			@Override
			public void messageReceived (Message msg) {
				gEngine.startGame ();
				String nextPlayer = gEngine.getNextTurn ();
				Logger.logDebug ("Current turn: " + nextPlayer);
				for (CommunicationHandler ch : clientList) {
					ch.sendData (msg);
					ch.sendData (MessageTag.CURRENT_TURN, nextPlayer);
				}
			}
		});
		msgHandler.registerReceiveMessageListener (MessageTag.RESPONSE, new MessageListener () {
			@Override
			public void messageReceived (Message msg) {
				if (msg.from.equals (gEngine.getCurrentTurn ())) {
					String nextPlayer = gEngine.getNextTurn ();
					Logger.logDebug ("Current turn: " + nextPlayer);
					for (CommunicationHandler ch : clientList) {
						ch.sendData (msg);
						ch.sendData (MessageTag.CURRENT_TURN, nextPlayer);
					}
				}
				else {
					Logger.log ("Not " + msg.from + "'s turn");
				}
			}
		});
		msgHandler.registerReceiveMessageListener (MessageTag.SKIP_TURN, new MessageListener () {
			@Override
			public void messageReceived (Message msg) {
				String nextPlayer = gEngine.getNextTurn ();
				for (CommunicationHandler ch : clientList) {
					ch.sendData (MessageTag.CURRENT_TURN, nextPlayer);
				}
			}
		});
	}

	private Thread createListenThread () {
		return new Thread (new Runnable () {
			@Override
			public void run () {
				try {
					Logger.log ("Listening for clients...");
					while (true) {
						Socket clientSocket = serverSocket.accept ();
						Logger.log ("Client connecting...");

						//Put client on new thread
						try {
							threadPool.execute (new ConnectionHandler (clientSocket, gEngine, msgHandler, clientList));
						}
						catch (Exception ex) {
							Logger.logException ("Server::createListenThread", ex);
						}
					}
				}
				catch (SocketException sEx) {
					//Cleanup
					Logger.log ("Disconnecting clients...");
					for (CommunicationHandler ch : clientList) {
						ch.sendData (MessageTag.EXIT, "");
					}
					threadPool.shutdown ();

					//Disconnect all (will purge list)
					for (int i = 0; i < clientList.size (); i++) {
						clientList.get (0).disconnect ();
					}

				}
				catch (Exception ex) {
					Logger.logException ("Server::createListenThread", ex);
				}
			}
		});
	}

	private Thread createCmdThread () {
		return new Thread (new Runnable () {
			private String cmd;
			private boolean read = true;
			BufferedReader reader = new BufferedReader (new InputStreamReader (System.in));

			@Override
			public void run () {
				try {
					while (read) {
						cmd = reader.readLine ();

						switch (cmd) {
							case "exit":
								serverSocket.close ();
								System.exit (0);
								break;
						}
					}
				}
				catch (Exception ex) {
					Logger.logException ("Server::createCmdThread", ex);
				}
				finally {
					try {
						reader.close ();
					}
					catch (Exception ex) {
					}
				}
			}
		});
	}
}
