package client;

interface FOConsumer {
	void accept(String s) throws Client.BadPermissionsException;
}