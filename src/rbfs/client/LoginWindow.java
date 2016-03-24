package rbfs.client;

import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;
import javax.swing.*;

/**
This is the first window seen by users just starting up the RBFS client. It is
used to login to remote RBFS servers, or as a landing page if the user is
disconnected from a server.

@author	James Hoak
*/

class LoginWindow extends JFrame {

	private static final String PREFS_FILE = "userPrefs.ini"; // the file to get
															  // user prefs from
	private static final int WIDTH = 350,					  // width and
							 HEIGHT = 200;					  // height of the window

	private JTextField nameField, // contains the username,
					   passField, // password,
					   ipField,   // IP to connect to, and
					   portField; // the port number to use
	
	private JLabel cnxnLabel;	  // The label that states the IP and port to connect to
	private JButton connectBtn;   // The button used to connect to a RBFS server
	private UserPrefs prefs;	  // The user's preferences to use in the fields here
	
	/**
	Creates a new window, given a method to connect to a remote server.
	@param connectMethod The method to use to connect.
	*/
	private LoginWindow(Consumer<UserPrefs> connectMethod) {
		// Window options
		setTitle("RBFS Login");
		setSize(WIDTH, HEIGHT);
		setResizable(false);
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(
			(int)(screenDim.getWidth() / 2.0 - WIDTH / 2), 
			(int)(screenDim.getHeight() / 2.0 - HEIGHT / 2)
		);
		setLayout(new CardLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// Attempt to load prefs. If we fail, then just use some default prefs (below).
		try {
			prefs = UserPrefs.load(PREFS_FILE);
		} catch (UserPrefs.LoadException x) {
			try {
				prefs = UserPrefs.make(
					"Name",			// name
					"Password",		// password
					"127.0.0.1",	// IP
					"5001" 			// port
				);
			}
			catch (UserPrefs.InvalidPrefsException y) {
				// Can't fail. No reason to do anything here.
			}
		}
		
		initConnectButton(connectMethod);
		add(makeFrontPanel());
		add(makeBackPanel());
		getRootPane().setDefaultButton(connectBtn);
	}

	/**
	Factory method to create a new window.
	@param connectMethod The method to use to connect to a remote server.
	@return Returns a new login window.
	*/
	static LoginWindow make(Consumer<UserPrefs> connectMethod) {
		return new LoginWindow(connectMethod);
	}

	/**
	Initialize the button used to connect to a server.
	@param connectMethod The method to use to connect to a remote server.
	*/
	private void initConnectButton(Consumer<UserPrefs> connectMethod) {
		connectBtn = new JButton("Connect");
		connectBtn.addActionListener(new ConnectButtonListener(connectMethod));
	}

	/**
	Make the panel where the user can enter their username and password.
	@return The front part of the window, as a Box.
	*/
	private Box makeFrontPanel() {
		nameField = makeTextField(prefs.getName(), false);
		passField = makeTextField(prefs.getPassword(), true);
		JPanel userPanel = makeInputPanel("Username", nameField),
			   passPanel = makeInputPanel("Password", passField),
			   cnxnPanel = new JPanel(),
			   buttonPanel = makeFrontButtonPanel();
		cnxnLabel = new JLabel("Connecting to " + prefs.getIP() + ":" + prefs.getPort() + "...");
		cnxnPanel.add(cnxnLabel);
		
		return makeMainPanel(userPanel, passPanel, cnxnPanel, buttonPanel);
	}

	/**
	Make the panel where the user can enter the IP and port num. to connect to.
	@return The back part of the login window, as a Box.
	*/
	private Box makeBackPanel() {
		ipField = makeTextField(prefs.getIP(), false);
		portField = makeTextField(prefs.getPort() + "", false);
		JPanel ipPanel = makeInputPanel("IP", ipField),
			   portPanel = makeInputPanel("Port", portField),
			   buttonPanel = makeBackButtonPanel();

		return makeMainPanel(ipPanel, portPanel, buttonPanel);
	}

	/**
	Makes a new Box and adds the given components to it before returning it.
	@param comps The components to add
	@return The resulting Box after adding the given components
	*/
	private Box makeMainPanel(Component... comps) {
		Box mainBox = new Box(BoxLayout.Y_AXIS);
		for (Component c : comps) {
			JPanel panel = new JPanel(new GridBagLayout());
			panel.add(c);
			mainBox.add(panel);
		}
		return mainBox;
	}

	/**
	Given the name of an input field and the field itself, constructs and returns
	a panel for it.
	@param fieldName The name of the field (Username, Password, etc.)
	@param inputField The input field to use
	@return A new panel labeled with the field's name and with the field itself
	*/
	private JPanel makeInputPanel(String fieldName, JTextField inputField) {
		JLabel label = new JLabel(fieldName + ": ", SwingConstants.CENTER);
		JPanel panel = new JPanel();
		panel.add(label);
		panel.add(inputField);
		return panel;
	}

	/**
	Creates the buttons on the front part of the window, and returns as a panel.
	@return A panel containing the buttons on the front of the login window.
	*/
	private JPanel makeFrontButtonPanel() {
		JButton quitBtn = new JButton("Quit"),
				optionsBtn = new JButton("Options");
		
		quitBtn.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										System.exit(0);
									}
								  });
		optionsBtn.addActionListener(new MenuChangeListener());

		return makeButtonPanel(quitBtn, connectBtn, optionsBtn);
	}

	/**
	Creates the buttons on the back part of the window, and returns as a panel.
	@return A panel containing the buttons on the second menu of the login window.
	*/
	private JPanel makeBackButtonPanel() {
		JButton backBtn = new JButton("Back");
		backBtn.addActionListener(new MenuChangeListener());
		return makeButtonPanel(backBtn);
	}

	/**
	Makes a new panel of buttons, given the buttons to add to the panel.
	@param buttons The buttons to add
	@return A new panel containing the buttons
	*/
	private JPanel makeButtonPanel(JButton... buttons) {
		JPanel panel = new JPanel();
		for (JButton b : buttons)
			panel.add(b);
		return panel;
	}

	/**
	Makes a text field (or password field if specified) and returns it.
	@param input The input to put in the field by default
	@param isPassworded Whether or not the field should hide the actual characters.
	@return A new text field (or password field) with the given input
	*/
	private JTextField makeTextField(String input, boolean isPassworded) {
		JTextField field = isPassworded ? new JPasswordField(input) : new JTextField(input);
		field.setColumns(10);
		return field;
	}

	/**
	Returns the content pane for the options button in the window.
	@param optionsBtn The button to show the options (connection info) menu.
	@return The content pane that contains the options button.
	*/
	private JPanel getContentPane(JButton optionsBtn) {
		Component comp = optionsBtn.getParent();
		for (int i = 0; i < 3; i++) 
			comp = comp.getParent();
		return (JPanel)comp;
	}

	/**
	A listener that waits for the menus to switch (from name and password to IP and port)
	and changes the front panel's label if so.
	*/
	private class MenuChangeListener implements ActionListener {
		
		/**
		If the IP and port are invalid when changing to the first menu, force the user to
		provide valid info. Otherwise, allow the switch to the other menu.
		@param e The actionEvent that fired this listener
		*/
		public void actionPerformed(ActionEvent e) {
			String ip = ipField.getText(),
				   port = portField.getText();
			if (!UserPrefs.isValidIP(ip) || !UserPrefs.isValidPort(port)) {
				JOptionPane.showMessageDialog(null, "Error: Please enter a valid IP and a port between 0 and 65535, inclusive.");
				return;
			}

			cnxnLabel.setText("Connecting to " + ip + ":" + port + "...");
			JButton button = (JButton)e.getSource();
			JPanel pane = getContentPane(button);
			CardLayout layout = (CardLayout)pane.getLayout();
			layout.next(pane);
		}
	}

	/**
	Fires when the user attempts to make a connection using the given connectMethod.
	*/
	private class ConnectButtonListener implements ActionListener {
		
		private Consumer<UserPrefs> connectMethod;

		/**
		Creates a new listener with the given connect method.
		@param connectMethod The method to use when trying to connect to the remote RBFS server.
		*/
		ConnectButtonListener(Consumer<UserPrefs> connectMethod) {
			this.connectMethod = connectMethod;
		}

		/**
		Attempts to connect to an RBFS server using the user's provided information.
		If the preferences are invalid, shows the user a dialog to alert them.
		Also saves the user's preferences to their local disk.
		@param e The event (clicking the Connect button) that fired this listener.
		*/
		public void actionPerformed(ActionEvent e) {
			String name = nameField.getText(),
				   pass = passField.getText(),
				   ip = ipField.getText(),
			  	   port = portField.getText();
			try {
				UserPrefs newPrefs = UserPrefs.make(name, pass, ip, port);
				prefs = newPrefs;
				prefs.save(PREFS_FILE);

				connectMethod.accept(prefs);
			}
				
			catch (UserPrefs.InvalidPrefsException x) {
				JOptionPane.showMessageDialog(null, x.getMessage());
			}
		}
	}
}
