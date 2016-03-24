package rbfs.client;

import java.io.*;
import java.net.*;
import java.util.function.*;
import java.util.LinkedList;
import javax.swing.JOptionPane;
import rbfs.client.fcn.*;
import rbfs.client.util.BadPermissionsException;

/**
The main class used by the client program. Running this class by default opens
a window for the user to log in, though the main job of this class is to handle
networking and manage the various windows that are part of the application.

@author	James Hoak
*/

public class Client {

	// Window components to manage
	private static LoginWindow login;
	private static FileMenu fileMenu;
	private static FileViewer viewer;

	// Connection var, other info
	private static Socket cnxn;
	private static LinkedList<String> activeRoles;
	private static String viewedFile;

	/**
	Main just starts up the login window. 99% of the backend work done by the
	client happens in methods described below.
	@param args The command line arguments (not used).
	*/
	public static void main(String[] args) {

		// Tell the login window what to do when the user wants to connect
		Consumer<UserPrefs> connectMethod = (prefs) -> connect(prefs);
		login = LoginWindow.make(connectMethod);
		login.setVisible(true);
	}

	/**
	Attempts to connect to the RBFS server at the user's specified IP and port,
	using the given login info. If successful, opens a new FileMenu so the user
	can pick their roles and access files. Shows an error message if the
	connection attempt fails somehow.
	@param prefs The user's preferences (name, password, IP, port)
	*/
	private static void connect(UserPrefs prefs) {
		try {
			String name = prefs.getName(),
				   pass = prefs.getPassword(),
				   ip = prefs.getIP(),
				   port = prefs.getPort();

			// Try to make our connection
			InetAddress addr = InetAddress.getByName(ip);
			cnxn = new Socket(addr, Integer.parseInt(port));
			cnxn.setSoTimeout(10000);

			// Send in login info to be authenticated
			sendMessage(name + "\n" + pass + "\n");
			checkInitialResponse(getResponseHead());
			
			// Get roles (if successful)
			String rest = getRestOfResponse();
			LinkedList<String> roles = getRoles(rest);
			
			// Make methods to perform operations for the file menu
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

	/**
	Turns a String message into raw bytes and sends them to the server.
	@param message The message to send to the server
	@throws IOException If an I/O error occurs
	*/
	private static void sendMessage(String message) throws IOException {
		byte[] byteMsg = message.getBytes();
		OutputStream out = cnxn.getOutputStream();
		out.write(byteMsg);
		out.flush();
	}

	/**
	Gets the first "token" of the server's response (i.e. the text before the
	first \n character).
	@return The first part of the server's response
	@throws IOException If an I/O error occurs
	@throws SocketTimeoutException If the socket read() call times out
	*/
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

	/**
	If the message is an error message, throws a FailedLoginException (because
	the login did indeed fail). Otherwise, does nothing.
	@param message The message (server response).
	@throws FailedLoginException If the server's response is an error message
	*/
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

		// If we came up with an error, throw the exception.
		if (!errorMessage.equals(""))
			throw new FailedLoginException(errorMessage);
	}

	/**
	Gets the rest of the response after the head.
	@return The rest of the server's response.
	@throws IOException If an I/O error occurs
	@throws SocketTimeoutException If the read call on the socket fails
	*/
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

	/**
	Used with the server's initial response (if it wasn't an error message).
	Gets the rest of the response as a linked list of Strings, which in this
	context are the roles the user may activate.
	@param rest The rest of the server's response
	@return A list of roles
	*/
	private static LinkedList<String> getRoles(String rest) {
		String[] roleArr = rest.split("\n");
		LinkedList<String> roles = new LinkedList<>();
		
		for (int i = 0; i < roleArr.length; i++)
			roles.add(roleArr[i]);
		
		return roles;
	}

	/**
	Called if we're done interacting with the server. Starts and finishes the
	teardown process as well as closes all menus still around from a previous
	login.
	*/
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

	/**
	Attempts to "open" a file on the server using the user's current set of
	permissions. If successful, opens a new FileViewer window to display its
	text.
	@param fileName The name of the file to open
	@throws BadPermissionsException If the server says it's inaccessible.
	*/
	private static void openFile(String fileName) throws BadPermissionsException {
		try {
			// Ask for the file first!
			sendMessage("OPEN FILE " + fileName.toUpperCase() + (char)-1);
		
			// Now, check for success
			String responseHead = getResponseHead();
			if (responseHead.equals("ERROR: FILE NOT FOUND"))
				showError("File inaccessible.");
			else if (responseHead.equals("ERROR: BAD PERMISSIONS"))
				throw new BadPermissionsException("Error: File inaccessible with current permissions.");
			
			// If successful, close whatever file we have open now and make a
			// new window to display the new file.
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
		// If any I/O or timeout errors happen, have to initiate teardown
		catch (SocketTimeoutException x) {
			logout();
			showError("The server failed to respond.");
		}
		catch (IOException x) {
			logout();
			showError("Failed to communicate with the server.");
		}
	}

	/**
	Tries to save a file that the user has edited on their machine to the server.
	If successful, the server will overwrite its copy of the file.
	@param newContents The new contents of the file to be saved
	@throws BadPermissionsException If the user has insufficient permissions to
	save the file
	*/
	private static void saveFile(String newContents) throws BadPermissionsException {
		try {
			sendMessage("SAVE FILE\n" + viewedFile.toUpperCase() + "\n" + newContents + "\n" + (char)-1);

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

	/**
	Gets the file metadata (as one big String) for all files viewable/editable
	with the user's current set of roles.
	@param roles The user's active roles
	@return A String containing all accessible files' metadata
	@throws BadPermissionsException If the user may not access the file
	@throws IOException If an I/O error occurs
	*/
	private static String getFileInfo(LinkedList<String> roles) throws BadPermissionsException, IOException {
		try {
			exitViewer();
			// Ask for our file info
			sendMessage(makeFileInfoCommand(roles));

			// Interpret the response
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

	/**
	Convenience method to make a request to the server for info about accessible
	files.
	@param roles The list of active roles
	@return The command to send to the server
	*/
	private static String makeFileInfoCommand(LinkedList<String> roles) {
		String command = "GET FILES FOR ROLES";
		
		for (String s : roles)
			command = command + "\n" + s.toUpperCase();
		
		command = command + ((char)-1);
		return command;
	}

	/**
	Convenience method to display a message dialog when an error occurs.
	@param message The error message (should not start with "Error")
	*/
	private static void showError(String message) {
		JOptionPane.showMessageDialog(null, "Error: " + message);
	}

	/**
	If we are already viewing a file, this closes the view.
	*/
	private static void exitViewer() {
		if (viewer != null) {
			viewer.setVisible(false);
			viewer = null;
		}
	}

	/**
	Exception that represents a failed login attempt.
	*/
	private static class FailedLoginException extends Exception {

		/**
		Creates a new empty exception.
		*/
		FailedLoginException() {
			this(null);
		}

		/**
		Creates an exception with the given error message.
		@param message The error message to display
		*/
		FailedLoginException(String message) {
			super(message);
		}
	}
}
