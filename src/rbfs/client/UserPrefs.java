package rbfs.client;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
A convenience class, UserPrefs helps the LoginWindow class handle user
preferences (in terms of login/connection info). Saving and loading is done
from a local file on the user's computer, if the file is present. (It is
created if the user logs in successfully to another RBFS server, saving the
info from the successful login as a new file.)

@author	James Hoak
*/

class UserPrefs {

	private String name, pass, ip, port;

	/**
	Private constructor. Initializes the fields using the given info.
	@param name The users' name
	@param pass The password
	@param ip The IP to connect to
	@param port The port to connect to
	*/
	private UserPrefs(String name, String pass, String ip, String port) {
		this.name = name;
		this.pass = pass;
		this.ip = ip;
		this.port = port;
	}

	/**
	Makes a new set of preferences for the user given their preference info.
	@param name The users' name
	@param pass The password
	@param ip The IP to connect to
	@param port The port to connect to
	@return A new set of preferences for the user
	@throws InvalidPrefsException If the user provides a non-alphanumeric name
	or password, or an invalid IP or port number.
	*/
	static UserPrefs make(String name, String pass, String ip, String port) throws InvalidPrefsException {
		checkValid(name, pass, ip, port);
		return new UserPrefs(name, pass, ip, port);
	}

	/**
	Returns the user's name
	@return The user's name
	*/
	String getName() {
		return name;
	}

	/**
	Returns the user's password
	@return The user's password
	*/
	String getPassword() {
		return pass;
	}

	/**
	Returns the IP to which the user would prefer to connect
	@return The IP to connect to
	*/
	String getIP() {
		return ip;
	}

	/**
	Returns the port to which the user would prefer to connect
	@return The port to connect to
	*/
	String getPort() {
		return port;
	}

	/**
	Checks that the username and password are alphanumeric, as well as if the
	IP and port number are valid.
	@param name The username
	@param pass The password
	@param ip The IP
	@param port The port number
	@throws InvalidPrefsException If one or more of the preferences are invalid
	*/
	private static void checkValid(String name, String pass, String ip, String port) throws InvalidPrefsException {
		if (!isAlphanumeric(name))
			throw new InvalidPrefsException("Invalid name: " + name);

		if (!isAlphanumeric(pass))
			throw new InvalidPrefsException("Invalid password: " + pass);

		if (!isValidIP(ip))
			throw new InvalidPrefsException("Invalid IP: " + ip);

		if (!isValidPort(port))
			throw new InvalidPrefsException("Invalid port: " + port);
	}

	/**
	Checks whether a string is alphanumeric.
	@param s The string to test
	@return True if the string has only numbers and letters, or false otherwise
	*/
	static boolean isAlphanumeric(String s) {
		for (char c : s.toCharArray()) {
			if (!Character.isLetterOrDigit(c))
				return false;
		}

		if (s.equals(""))
			return false;

		return true;
	}

	/**
	Checks whether an IP is valid.
	@param ip The IP to test
	@return True if the IP is valid, and false otherwise
	*/
	static boolean isValidIP(String ip) {
		try {
			InetAddress.getByName(ip);
			return true;
		} 
		catch (UnknownHostException x) {
			return false;
		}
	}

	/**
	Checks whether a port number is valid.
	@param port The port number to test
	@return True if the port number is an int between 0 and 65535 inclusive, or
	false otherwise.
	*/
	static boolean isValidPort(String port) {
		try {
			int portnum = Integer.parseInt(port);
			if (portnum >= 0 && portnum <= 65535)
				return true;
			else
				return false;
		} 
		catch (NumberFormatException x) {
			return false;
		}
	}

	/**
	Attempts to save the preferences file to disk.
	@param filename The file to save preferences to
	*/
	void save(String filename) {
		try {
			FileWriter fw = new FileWriter(filename);
			fw.write("Username=" + name + "\nPassword=" + pass + "\nIP=" + ip + "\nPort=" + port + "\n");
			fw.close();
		} catch (IOException x) {
			// Do nothing. User can just input their preferences again later. No big!
		}
	}

	/**
	Attempts to load the local preferences file and return a new set of
	preferences that reflect those in the file.
	@param filename The name of the local file
	@return A new set of preferences that reflect the ones in the file
	@throws LoadException If the method fails to load the file
	*/
	static UserPrefs load(String filename) throws LoadException {
		try {
			// Read in preferences as a String
			FileReader fr = new FileReader(filename);
			StringBuilder sb = new StringBuilder();
			int i = fr.read();
			while (i != -1) {
				sb.append((char)i);
				i = fr.read();
			}
			fr.close();

			String fileContents = sb.toString();
			// If the format of the file is wrong, throw a LoadException
			if (!fileContents.matches("Username=(.*)(\\s+)Password=(.*)(\\s+)IP=(.*)(\\s+)Port=(0-9+)(\\s+)"))
				throw new LoadException("Invalid prefs file.");

			return loadFromString(fileContents);
		}
		catch (IOException x) {
			throw new LoadException("Failed to load preferences from file \"" + filename + "\".");
		}
	}

	/**
	Makes a set of preferences from the given String (called by load()).
	@param fileContents The contents of the file (formatted correctly)
	@return A set of preferences given by the file
	*/
	private static UserPrefs loadFromString(String fileContents) {
		String[] inputs = new String[4];
		for (int i = 0; i < inputs.length; i++) {
			inputs[i] = firstInput(fileContents);
			fileContents = cutNextLine(fileContents);
		}
		return new UserPrefs(
			inputs[0],
			inputs[1],
			inputs[2],
			inputs[3]
		);
	}

	/**
	Returns the first preference given in the provided String
	@param str The string from which to get the next input
	@return The first preference setting
	*/
	private static String firstInput(String str) {
		String line = nextLine(str);
		return line.substring(line.indexOf('=') + 1, line.length());
	}

	/**
	Returns the next preference line of the file
	@param str The string from which to get the next line
	@return Returns the first line of the rest of the file
	*/
	private static String nextLine(String str) {
		int i = 0;
		while (str.charAt(i) != '\n' && str.charAt(i) != '\r')
			i++;
		return str.substring(0, i);
	}

	/**
	Returns the input string with the first line removed.
	@param str The string whose first line we want removed
	@return The string with the first line removed
	*/
	private static String cutNextLine(String str) {
		return str.substring(str.indexOf('\n') + 1, str.length());
	}

	/**
	Represents an error generated by an invalid username, password, IP, or port
	number.
	*/
	static class InvalidPrefsException extends Exception {

		/**
		Creates an exception with an empty message.
		*/
		InvalidPrefsException() {
			this(null);
		}

		/**
		Creates an exception with the given message.
		@param message The error message to use
		*/
		InvalidPrefsException(String message) {
			super(message);
		}
	}

	/**
	Represents an error that occurred while trying to load preferences from a
	file.
	*/
	static class LoadException extends FileNotFoundException {

		/**
		Creates an exception with an empty message.
		*/
		LoadException() {
			this(null);
		}

		/**
		Creates an exception with the given message.
		@param message The error message to use
		*/
		LoadException(String message) {
			super(message);
		}
	}
}
