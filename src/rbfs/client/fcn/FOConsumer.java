package rbfs.client.fcn;

import rbfs.client.util.BadPermissionsException;

/**
In general, this interface stands for a Consumer<String> but really is used to
open files in the client ("FileOpenConsumer" or FOConsumer for short).

@author	James Hoak
*/

@FunctionalInterface
public interface FOConsumer {
	/**
	Calls a method that simply takes a String as a parameter and returns
	nothing.
	@param s The string argument
	@throws BadPermissionException If the permissions are not high enough to
	perform the requested operation.
	*/
	public void accept(String s) throws BadPermissionsException;
}
