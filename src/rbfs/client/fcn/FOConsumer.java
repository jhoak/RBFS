package rbfs.client.fcn;

import rbfs.client.BadPermissionsException;

@FunctionalInterface
interface FOConsumer {
	void accept(String s) throws BadPermissionsException;
}
