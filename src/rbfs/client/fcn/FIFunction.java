package rbfs.client.fcn;

import java.io.IOException;
import java.util.LinkedList;
import rbfs.client.util.BadPermissionsException;

/**
This generally represents a method that takes a linked list of Strings and
returns a String, but in the client this interface is short for a method that
takes a list of user roles, sends them to the server, and gets a response that
indicates either failure or success (with file metadata being sent on success).

@author	James Hoak
*/

@FunctionalInterface
public interface FIFunction {
	/**
	Uses a LinkedList of Strings to call a method and returns a String value.
	@param list The list of Strings
	@return Returns the String result of performing the given operation.
	@throws BadPermissionsException If the permissions are not high enough.
	@throws IOException If acquiring input leads to an I/O error of some sort.
	*/
	public String accept(LinkedList<String> list) throws BadPermissionsException, IOException;
}
