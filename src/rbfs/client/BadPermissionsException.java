package rbfs.client;

public class BadPermissionsException extends Exception {

	public BadPermissionsException() {
		this(null);
	}

	public BadPermissionsException(String message) {
		super(message);
	}
}