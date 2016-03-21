package rbfs.client.fcn;

import rbfs.client.util.BadPermissionsException;

@FunctionalInterface
public interface FOConsumer {
	public void accept(String s) throws BadPermissionsException;
}
