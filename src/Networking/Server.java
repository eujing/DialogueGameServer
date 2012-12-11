package Networking;

import Game.GameEngine;
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
	private ServerMessageHandler msgHandler;
	private GameEngine gEngine;

	public Server (short port) throws IOException {
		this.serverSocket = new ServerSocket (port);
		this.clientList = Collections.synchronizedList (new ArrayList<CommunicationHandler> ());
		this.threadPool = Executors.newFixedThreadPool (N_THREADS);
		this.gEngine = new GameEngine ();
		this.msgHandler = new ServerMessageHandler (this.clientList, this.gEngine);
		
		//Connection listener
		this.listenThread = new Thread (new Runnable () {
			@Override
			public void run () {
				try {
					System.out.println ("Listening for clients...");
					while (true) {
						Socket clientSocket = serverSocket.accept ();
						System.out.println ("Client connecting...");

						//Put client on new thread
						try {
							threadPool.execute (new ConnectionHandler (clientSocket, msgHandler, clientList));
							System.out.println ("here");
						}
						catch (Exception ex) {
							System.out.println (ex.getMessage ());
						}
					}
				}
				catch (SocketException sEx) {
					//Cleanup
					System.out.println ("Disconnecting clients...");
					for (CommunicationHandler ch : clientList) {
						ch.sendData ("exit", "");
					}
					threadPool.shutdown ();

					//Disconnect all (will purge list)
					for (int i = 0; i < clientList.size (); i++) {
						clientList.get (0).disconnect ();
					}

				}
				catch (Exception ex) {
					System.out.println ("Server listenThread: " + ex.getMessage ());
				}
			}
		});

		//Listen for commands
		new Thread (new Runnable () {
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
					System.out.println (ex.getMessage ());
				}
				finally {
					try {
						reader.close ();
					}
					catch (Exception ex) {
					}
				}
			}
		}).start ();
	}

	public void startListening () {
		this.listenThread.start ();
	}
}
