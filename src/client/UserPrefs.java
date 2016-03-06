package client;

import java.io.*;

class UserPrefs {

	private String name, pass, ip;
	private int port;

	private UserPrefs(String name, String pass, String ip, int port) {
		this.name = name;
		this.pass = pass;
		this.ip = ip;
		this.port = port;
	}

	static UserPrefs make(String name, String pass, String ip, int port) {
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

	int getPort() {
		return port;
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
			Integer.parseInt(inputs[3])
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
}