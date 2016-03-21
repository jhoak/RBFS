package rbfs.client.fcn;

import java.io.IOException;
import java.util.LinkedList;
import rbfs.client.util.BadPermissionsException;

@FunctionalInterface
public interface FIFunction {
	public String accept(LinkedList<String> list) throws BadPermissionsException, IOException;
}
