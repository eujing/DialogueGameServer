package Networking;

public interface ConnectionListener {

	public void onConnect (CommunicationHandler commHandler);

	public void onDisconnect (CommunicationHandler commHandler);
}
