package Networking;

public interface ConnectionListener {

	public void onConnect (CommunicationHandler commHandler) throws Exception;

	public void onDisconnect (CommunicationHandler commHandler);
}
