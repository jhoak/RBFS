package client;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;
import java.util.LinkedList;

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

			byte[] nameBytes = (name + '\n').getBytes(),
					passBytes = (pass + '\n').getBytes();
			OutputStream out = cnxn.getOutputStream();
			out.write(nameBytes);
			out.write(passBytes);
			out.flush();

			InputStreamReader in = new InputStreamReader(cnxn.getInputStream());

			StringBuilder sb = new StringBuilder();
			LinkedList<String> roles = new LinkedList<>();
			int nextchar = in.read();
			while (nextchar != -1) {
				if (nextchar != '\n')
					sb.append((char)nextchar);
				else {
					roles.add(sb.toString());
					sb = new StringBuilder();
				}
				nextchar = in.read();
			}
			roles.add(sb.toString());

			FileMenu menu = FileMenu.make(roles);
			menu.setVisible(true);
			login.setVisible(false);

		} catch (Exception x) {
			// rekt
		}
	}
}