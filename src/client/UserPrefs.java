package client;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

class UserPrefs {

	private String name, pass, ip, port;

	private UserPrefs(String name, String pass, String ip, String port) {
		this.name = name;
		this.pass = pass;
		this.ip = ip;
		this.port = port;
	}

	static UserPrefs make(String name, String pass, String ip, String port) throws InvalidPrefsException {
		checkValid(name, pass, ip, port);
		return new UserPrefs(name, pass, ip, port);
	}

	String getName() {
		return name;
	}

	String getPassword() {
		return pass;
	}

	String getIP() {
		return ip;
	}

	String getPort() {
		return port;
	}

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

	static boolean isAlphanumeric(String s) {
		for (char c : s.toCharArray()) {
			if (!Character.isLetterOrDigit(c))
				return false;
		}

		if (s.equals(""))
			return false;

		return true;
	}

	static boolean isValidIP(String ip) {
		try {
			InetAddress.getByName(ip);
			return true;
		} 
		catch (UnknownHostException x) {
			return false;
		}
	}

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

	static UserPrefs load(String filename) throws LoadException {
		try {
			FileReader fr = new FileReader(filename);
			StringBuilder sb = new StringBuilder();
			int i = fr.read();
			while (i != -1) {
				sb.append((char)i);
				i = fr.read();
			}
			fr.close();

			String fileContents = sb.toString();
			if (!fileContents.matches("Username=(.*)(\\s+)Password=(.*)(\\s+)IP=(.*)(\\s+)Port=(0-9+)(\\s+)"))
				throw new LoadException("Invalid prefs file.");

			return loadFromString(fileContents);
		}
		catch (IOException x) {
			throw new LoadException("Failed to load preferences from file \"" + filename + "\".");
		}
	}

	void save(String filename) {
		try {
			FileWriter fw = new FileWriter(filename);
			fw.write("Username=" + name + "\nPassword=" + pass + "\nIP=" + ip + "\nPort=" + port + "\n");
			fw.close();
		} catch (IOException x) {
			// Do nothing. User can just input their preferences again later. No big!
		}
	}

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

	private static String firstInput(String str) {
		String line = nextLine(str);
		return line.substring(line.indexOf('=') + 1, line.length());
	}

	private static String nextLine(String str) {
		int i = 0;
		while (str.charAt(i) != '\n' && str.charAt(i) != '\r')
			i++;
		return str.substring(0, i);
	}

	private static String cutNextLine(String str) {
		return str.substring(str.indexOf('\n') + 1, str.length());
	}

	static class InvalidPrefsException extends Exception {

		InvalidPrefsException() {
			this(null);
		}

		InvalidPrefsException(String message) {
			super(message);
		}
	}

	static class LoadException extends FileNotFoundException {

		LoadException() {
			this(null);
		}

		LoadException(String message) {
			super(message);
		}
	}
}
