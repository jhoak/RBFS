package rbfs.client.fcn;

import java.io.IOException;
import java.util.LinkedList;
import rbfs.client.BadPermissionsException;

@FunctionalInterface
interface FIFunction {
	String accept(LinkedList<String> list) throws BadPermissionsException, IOException;
}
