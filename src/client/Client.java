package client;

import java.io.*;
import java.net.*;
import java.util.function.*;
import java.util.LinkedList;
import javax.swing.JOptionPane;

public class Client {

	private static LoginWindow login;
	private static FileMenu fileMenu;
	private static FileViewer viewer;
	private static Socket cnxn;
	private static LinkedList<String> activeRoles;
	private static String viewedFile;

	public static void main(String[] args) {
		Consumer<UserPrefs> connectMethod = (prefs) -> connect(prefs);
		login = LoginWindow.make(connectMethod);
		login.setVisible(true);
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

			sendMessage(name + "\n" + pass + "\n");
			checkInitialResponse(getResponseHead());
			
			String rest = getRestOfResponse();
			LinkedList<String> roles = getRoles(rest);
			
			Runnable logoutMethod = Client::logout;
			FOConsumer openMethod = (s) -> openFile(s);
			FIFunction fileInfoMethod = (list) -> getFileInfo(list);
			
			fileMenu = FileMenu.make(roles, logoutMethod, fileInfoMethod, openMethod);
			fileMenu.setVisible(true);
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
		catch (Exception x) {
			showError("An unknown error occurred.");
		}
	}

	private static void sendMessage(String message) throws IOException {
		byte[] byteMsg = message.getBytes();
		OutputStream out = cnxn.getOutputStream();
		out.write(byteMsg);
		out.flush();
	}

	private static String getResponseHead() throws IOException, SocketTimeoutException {
		InputStreamReader in = new InputStreamReader(cnxn.getInputStream());
		StringBuilder sb = new StringBuilder();
		
		int ch = in.read();
		while (ch != '\n') {
			sb.append((char)ch);
			ch = in.read();
		}
		return sb.toString();
	}

	private static void checkInitialResponse(String message) throws FailedLoginException {
		String errorMessage = "";
		
		if (message.equals("ERROR: BAD LOGIN")) {
			errorMessage = "Unrecognized username/password combination.";
		}
		else if (message.equals("ERROR: NO ROLES")) {
			errorMessage = "No roles assigned to user.";
		}
		else if (!message.equals("OK")) {
			errorMessage = "Unknown error occurred.";
		}

		if (!errorMessage.equals(""))
			throw new FailedLoginException(errorMessage);
	}

	private static String getRestOfResponse() throws IOException, SocketTimeoutException {
		InputStreamReader in = new InputStreamReader(cnxn.getInputStream());
		StringBuilder sb = new StringBuilder();
		
		int ch = in.read();
		while (ch != -1) {
			sb.append((char)ch);
			ch = in.read();
		}
		return sb.toString();
	}

	private static LinkedList<String> getRoles(String rest) {
		String[] roleArr = rest.split("\n");
		LinkedList<String> roles = new LinkedList<>();
		
		for (int i = 0; i < roleArr.length; i++)
			roles.add(roleArr[i]);
		
		return roles;
	}

	private static void logout() {
		try {
			cnxn.close();
		} catch (IOException x) {
			/* Do nothing and move on (socket is unusable anyway, so just make 
			   a new connection later if necessary) */
		}
		fileMenu.setVisible(false);
		fileMenu = null;
		cnxn = null;
		exitViewer();
		login.setVisible(true);
	}

	private static void openFile(String fileName) throws BadPermissionsException {
		try {
			sendMessage("OPEN FILE " + fileName.toUpperCase() + (char)-1);
		
			String responseHead = getResponseHead();
			if (responseHead.equals("ERROR: FILE NOT FOUND"))
				showError("File inaccessible.");
			else if (responseHead.equals("ERROR: BAD PERMISSIONS"))
				throw new BadPermissionsException("Error: File inaccessible with current permissions.");
			else {
				exitViewer();
				boolean editable = Boolean.parseBoolean(getResponseHead());
				String contents = getRestOfResponse();
				
				FOConsumer saveFcn = (s) -> saveFile(s);
				viewer = FileViewer.make(editable, contents, saveFcn);
				viewer.setVisible(true);
				viewedFile = fileName;
			}
		}
		catch (SocketTimeoutException x) {
			logout();
			showError("The server failed to respond.");
		}
		catch (IOException x) {
			logout();
			showError("Failed to communicate with the server.");
		}
	}

	private static void saveFile(String newContents) throws BadPermissionsException {
		try {
			sendMessage("SAVE FILE " + viewedFile.toUpperCase() + (char)-1);

			String responseHead = getResponseHead();
			if (responseHead.equals("ERROR: BAD PERMISSIONS"))
				throw new BadPermissionsException("Error: File unable to be edited with current permissions.");
		}
		catch (SocketTimeoutException x) {
			logout();
			showError("The server failed to respond.");
		}
		catch (IOException x) {
			logout();
			showError("Failed to communicate with the server.");
		}
	}

	private static String getFileInfo(LinkedList<String> roles) throws BadPermissionsException, IOException {
		try {
			exitViewer();
			sendMessage(makeFileInfoCommand(roles));

			String responseHead = getResponseHead();
			if (responseHead.equals("ERROR: NO ACCESSIBLE FILES"))
				throw new BadPermissionsException("Error: no files accessible with current roles.");

			String rest = getRestOfResponse();
			if (responseHead.equals("ERROR: ROLE CONFLICTS PRESENT")) {
				String errorMessage = "Error: Conflicting roles: " + rest;
				throw new BadPermissionsException(errorMessage);
			}
			
			activeRoles = roles;
			return rest;
		}
		catch (SocketTimeoutException x) {
			throw new IOException("Error: The server failed to respond.");
		}
		catch (IOException x) {
			throw new IOException("Error: Failed to communicate with the server.");
		}
	}

	private static String makeFileInfoCommand(LinkedList<String> roles) {
		String command = "GET FILES FOR ROLES";
		
		for (String s : roles)
			command = command + "\n" + s.toUpperCase();
		
		command = command + ((char)-1);
		return command;
	}

	private static void showError(String message) {
		JOptionPane.showMessageDialog(null, "Error: " + message);
	}

	private static void exitViewer() {
		if (viewer != null) {
			viewer.setVisible(false);
			viewer = null;
		}
	}

	private static class FailedLoginException extends Exception {

		FailedLoginException() {
			this(null);
		}

		FailedLoginException(String message) {
			super(message);
		}
	}

	static class BadPermissionsException extends Exception {

		BadPermissionsException() {
			this(null);
		}

		BadPermissionsException(String message) {
			super(message);
		}
	}
}
