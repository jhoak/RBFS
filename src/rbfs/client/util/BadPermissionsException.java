package rbfs.client.util;

/**
Exceptions of this type represent insufficient permissions on the user's side
that prevents the user from accessing (reading, writing, or both) a file hosted
on the server they are connected to.

@author	James Hoak
*/

public class BadPermissionsException extends Exception {

	/**
	Constructs a new exception with no message.
	*/
	public BadPermissionsException() {
		this(null);
	}

	/**
	Constructs a new exception with the given message.
	@param message The error message to use
	*/
	public BadPermissionsException(String message) {
		super(message);
	}
}