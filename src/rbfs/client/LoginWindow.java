package rbfs.client;

import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;
import javax.swing.*;
import rbfs.client.util.UserPrefs;

class LoginWindow extends JFrame {

	private static final String PREFS_FILE = "userPrefs.ini";
	private static final int WIDTH = 350,
							 HEIGHT = 200;

	private UserPrefs prefs;
	private JTextField nameField, passField, ipField, portField;
	private JLabel cnxnLabel;
	private JButton connectBtn;

	private LoginWindow(Consumer<UserPrefs> connectMethod) {
		setTitle("RBFS Login");
		setSize(WIDTH, HEIGHT);
		setResizable(false);
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int)(screenDim.getWidth() / 2.0 - WIDTH / 2), (int)(screenDim.getHeight() / 2.0 - HEIGHT / 2));
		setLayout(new CardLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);

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

	static LoginWindow make(Consumer<UserPrefs> connectMethod) {
		return new LoginWindow(connectMethod);
	}

	private void initConnectButton(Consumer<UserPrefs> connectMethod) {
		connectBtn = new JButton("Connect");
		connectBtn.addActionListener(new ConnectButtonListener(connectMethod));
	}

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

	private Box makeBackPanel() {
		ipField = makeTextField(prefs.getIP(), false);
		portField = makeTextField(prefs.getPort() + "", false);
		JPanel ipPanel = makeInputPanel("IP", ipField),
			   portPanel = makeInputPanel("Port", portField),
			   buttonPanel = makeBackButtonPanel();

		return makeMainPanel(ipPanel, portPanel, buttonPanel);
	}

	private Box makeMainPanel(Component... comps) {
		Box mainBox = new Box(BoxLayout.Y_AXIS);
		for (Component c : comps) {
			JPanel panel = new JPanel(new GridBagLayout());
			panel.add(c);
			mainBox.add(panel);
		}
		return mainBox;
	}

	private JPanel makeInputPanel(String fieldName, JTextField inputField) {
		JLabel label = new JLabel(fieldName + ": ", SwingConstants.CENTER);
		JPanel panel = new JPanel();
		panel.add(label);
		panel.add(inputField);
		return panel;
	}

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

	private JPanel makeBackButtonPanel() {
		JButton backBtn = new JButton("Back");
		backBtn.addActionListener(new MenuChangeListener());
		return makeButtonPanel(backBtn);
	}

	private JPanel makeButtonPanel(JButton... buttons) {
		JPanel panel = new JPanel();
		for (JButton b : buttons)
			panel.add(b);
		return panel;
	}

	private JTextField makeTextField(String input, boolean isPassworded) {
		JTextField field = isPassworded ? new JPasswordField(input) : new JTextField(input);
		field.setColumns(10);
		return field;
	}

	private JPanel getContentPane(JButton optionsBtn) {
		Component comp = optionsBtn.getParent();
		for (int i = 0; i < 3; i++) 
			comp = comp.getParent();
		return (JPanel)comp;
	}

	private class MenuChangeListener implements ActionListener {
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

	private class ConnectButtonListener implements ActionListener {
		
		private Consumer<UserPrefs> connectMethod;

		ConnectButtonListener(Consumer<UserPrefs> connectMethod) {
			this.connectMethod = connectMethod;
		}

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