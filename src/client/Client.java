package client;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;
import java.util.LinkedList;
import javax.swing.JOptionPane;

public class Client {

	private static LoginWindow login;
	private static Socket cnxn;

	public static void main(String[] args) {
		Consumer<UserPrefs> connectMethod = initConnectMethod();
		login = LoginWindow.make(connectMethod);
		login.setVisible(true);
	}

	private static Consumer<UserPrefs> initConnectMethod() {
		return (prefs) -> connect(prefs);
	}

	private static void connect(UserPrefs prefs) {
		try {
			String name = prefs.getName(),
				   pass = prefs.getPassword(),
				   ip = prefs.getIP(),
				   port = prefs.getPort();

			InetAddress addr = InetAddress.getByName(ip);
			cnxn = new Socket(addr, Integer.parseInt(port));
			cnxn.setSoTimeout(10000);

			OutputStream out = cnxn.getOutputStream();
			sendLogin(out, name, pass);

			InputStreamReader in = new InputStreamReader(cnxn.getInputStream());
			getServerMessage(in);

			LinkedList<String> roles = getRoles(in);
			FileMenu menu = FileMenu.make(roles);
			menu.setVisible(true);
			login.setVisible(false);

		}
		catch (FailedLoginException x) {
			showError(x.getMessage());
		}
		catch (SocketTimeoutException x) {
			showError("Connection timed out.");
		}
		catch (IOException x) {
			showError("Failed to connect.");
		}
	}

	private static void sendLogin(OutputStream out, String name, String pass) throws IOException {
		byte[] nameBytes = (name + '\n').getBytes(),
			   passBytes = (pass + '\n').getBytes();
		out.write(nameBytes);
		out.write(passBytes);
		out.flush();
	}

	private static void getServerMessage(InputStreamReader in) throws IOException, SocketTimeoutException, 
																					 FailedLoginException {
		StringBuilder sb = new StringBuilder();
		int nextch = in.read();
		while (nextch != '\n' && nextch != -1) {
			sb.append((char)nextch);
			nextch = in.read();
		}
		checkServerMessage(sb.toString());
	}

	private static void checkServerMessage(String message) throws FailedLoginException {
		String errorMessage = "";
		if (message.equals("BAD LOGIN")) {
			errorMessage = "Unrecognized username/password combination.";
		}
		else if (message.equals("NO ROLES")) {
			errorMessage = "No roles assigned to user.";
		}
		else if (!message.equals("OK")) {
			errorMessage = "Unknown error occurred.";
		}

		if (!errorMessage.equals(""))
			throw new FailedLoginException(errorMessage);
	}

	private static LinkedList<String> getRoles(InputStreamReader in) throws IOException, SocketTimeoutException {
		LinkedList<String> roles = new LinkedList<>();
		StringBuilder sb = new StringBuilder();
		int nextch = in.read();
		while (nextch != -1) {
			if (nextch != '\n')
				sb.append((char)nextch);
			else {
				roles.add(sb.toString());
				sb = new StringBuilder();
			}
			nextch = in.read();
		}
		roles.add(sb.toString());

		return roles;
	}

	private static void showError(String message) {
		JOptionPane.showMessageDialog(null, "Error: " + message);
	}

	private static class FailedLoginException extends Exception {

		FailedLoginException() {
			this(null);
		}

		FailedLoginException(String message) {
			super(message);
		}
	}
}