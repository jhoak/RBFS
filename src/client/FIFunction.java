package client;

import java.io.IOException;
import java.util.LinkedList;

interface FIFunction {
	String accept(LinkedList<String> list) throws Client.BadPermissionsException, IOException;
}
