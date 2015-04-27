import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.Date;

//This class both starts and maintains a window for users to log in to the server.
// If login succeeds, it starts a FileViewer window (code below as an inner class).
public class LoginWindow extends JFrame {

	private JTextField user;
	private JPasswordField pass;
	private Socket cnxn;
	private BufferedInputStream input;
	private BufferedOutputStream output;
	private InputStreamReader rdr;
	private OutputStreamWriter writer;
	private JLabel ipLabel;
	private String fileServerAddress = "127.0.0.1";
	private InetAddress addr;
	private String openFile;

	//Initializes a login window.
	public LoginWindow() {
		//Set window attributes
		setSize(350,220);
		setTitle("Login");
		setResizable(false);
		Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int)d.getWidth()/2 - 175, (int)d.getHeight()/2 - 110);
		setLayout(new GridLayout(5,1));

		//Create "Welcome" line at the top
		JLabel label = new JLabel("Welcome back! Please log in below:");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
		add(label);

		//Create panels for username/password fields
		JPanel p1 = new JPanel(),
			   p2 = new JPanel();
		JLabel l1 = new JLabel("Username:"),
		       l2 = new JLabel("Password:");
		user = new JTextField();
		pass = new JPasswordField();
		user.setColumns(20);
		pass.setColumns(20);
		p1.add(l1);
		p1.add(user);
		p2.add(l2);
		p2.add(pass);
		add(p1);
		add(p2);

		//Display IP that client will try to connect to
		ipLabel = new JLabel("Will attempt to connect to 127.0.0.1...");
		ipLabel.setHorizontalAlignment(SwingConstants.CENTER);
		ipLabel.setVerticalAlignment(SwingConstants.CENTER);
		add(ipLabel);

		//Create panel for buttons
		JPanel p3 = new JPanel();
		JButton connectTo = new JButton("Connect to..."),
				login = new JButton("Login"),
				quit = new JButton("Quit");
		connectTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String temp = JOptionPane.showInputDialog("Enter the IPv4 address of the server you'd like to connect to...");
				if (!temp.equals(""))
					fileServerAddress = temp;
				ipLabel.setText("Will attempt to connect to " + fileServerAddress + "...");
			}
		});
		login.addActionListener(new LoginListener());
		quit.addActionListener(new CloseListener());
		p3.add(connectTo);
		p3.add(login);
		p3.add(quit);
		add(p3);

		getRootPane().setDefaultButton(login);
		addWindowListener(new CloseListener());
		setVisible(true);
	}

	//Checks for illegal characters (i.e. non-alphanumeric). Used below in the login listener.
	public boolean hasIllegalChars(String s) {
		int j;
		for (int i = 0; i < s.length(); i++) {
			j = s.charAt(i);
			if (j < 48 || (j > 57 && j < 65) || (j > 90 && j < 97) || j > 122)
				return true;
		}
		return false;
	}

	//Listens for login attempts. If successful, starts a new FileViewer window.
	private class LoginListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			//Go back if user has not provided both U/N and password
			String un = user.getText(),
				   pw = new String(pass.getPassword());
			if (un.equals("") || pw.equals(""))
				JOptionPane.showMessageDialog(null, "Error: Please fill in both fields.");
			else if (hasIllegalChars(un) || hasIllegalChars(pw))
				JOptionPane.showMessageDialog(null, "Error: Bad login");
			else {
				//Send ID/PW combo to server to be verified.
				int port = 55554;
				try {
					//Establish connection and send U/N and password. Set up a connecting window in the meantime (see below).
					addr = InetAddress.getByName(fileServerAddress);
					if (!addr.isReachable(60000)) {
						JOptionPane.showMessageDialog(null, "Error: Cannot reach the specified address. Please check your destination IP and server settings.");
						return;
					}
					cnxn = new Socket(addr, port);
					output = new BufferedOutputStream(cnxn.getOutputStream());
					writer = new OutputStreamWriter(output);

					writer.write(un + (char)13);
					writer.write(pw + (char)13);
					writer.flush();


					//Get feedback from server.
					input = new BufferedInputStream(cnxn.getInputStream());
					rdr = new InputStreamReader(input);
					int i = rdr.read();
					if (i == 0) {
						//Failed login. Process input as error message and terminate.
						String msg = FSUtility.readConnectionInputToken(rdr);
						JOptionPane.showMessageDialog(null, msg);
						cnxn.close();
						return;
					}
					else {
						//Get number of roles
						int numRoles = Integer.parseInt(FSUtility.readConnectionInputToken(rdr));
						String[] roleNames = new String[numRoles];

						//Get role names.
						int j = 0;
						while (j < numRoles) {
							roleNames[j] = FSUtility.readConnectionInputToken(rdr);
							j++;
						}

						//Make sure user can read files
						int z = rdr.read();
						if (z == 0) {
							//Process input as error and terminate. No files readable.
							String msg = FSUtility.readConnectionInputToken(rdr);
							JOptionPane.showMessageDialog(null, msg);
							cnxn.close();
							return;
						}

						//Get number of files
						int numFiles = Integer.parseInt(FSUtility.readConnectionInputToken(rdr));

						String[] fileNames = new String[numFiles],
								 datesCreated = new String[numFiles],
							   	 datesModified = new String[numFiles],
								 authors = new String[numFiles];
						int[] sizes = new int[numFiles];
						boolean[] write = new boolean[numFiles];

						for (int k = 0; k < numFiles; k++) {
							//Fetch name, date created, date modified, author, size, and write perms. for each file.

							fileNames[k] = FSUtility.readConnectionInputToken(rdr);
							datesCreated[k] = FSUtility.readConnectionInputToken(rdr);
							datesModified[k] = FSUtility.readConnectionInputToken(rdr);
							authors[k] = FSUtility.readConnectionInputToken(rdr);
							sizes[k] = Integer.parseInt(FSUtility.readConnectionInputToken(rdr));

							// Get write permission
							i = rdr.read();
							write[k] = ((i == 0) ? false : true);
							i = rdr.read();
						}

						// All 6 arrays are now filled.

						FileViewer fv = new FileViewer(un, roleNames, fileNames, datesCreated, datesModified,
														authors, sizes, write);
						SwingUtilities.getWindowAncestor(user).setVisible(false);

					}

				}catch (Exception x) {
					x.printStackTrace();
					//JOptionPane.showMessageDialog(null, "Connection error: " + x.getMessage());
				}
			}
		}
	}

	//Displays files readable by the user. Also opens files on command.
	private class FileViewer extends JFrame {

		private String username, currentRole, selectedFile;
		private String[] roleList, fileList, datesCreated, datesModified, authorList, selectedRoles;
		private int[] sizes;
		private boolean[] write;
		private JList<String> list;
		private JLabel nameLabel, dateCLabel, dateMLabel, authLabel, sizeLabel, roleLabel;
		private JPanel filePanel, welcomePanel, deetsPanel, panel, fileDetails, openPanel;
		private String[] roles = {"admin","IT guy","CEO","waterboy","sr software engineer","sick coder","accountant",
										 "chief executive waterboy"};
		private JButton open;
		private int[][] roleConflicts;

		private FileViewer(String name, String[] role, String[] file,
						   String[] created, String[] modified, String[] auths,
						   int[] szs, boolean[] wrt) {

			// Set fields
			username = name;
			roleList = role;
			currentRole = roleList[0];
			fileList = file;
			datesCreated = created;
			datesModified = modified;
			authorList = auths;
			sizes = szs;
			write = wrt;
			selectedRoles = new String[roleList.length];
			selectedRoles[0] = currentRole;
			roleConflicts = new int[][]{{0,1,0,0,0,0,1,1},
							 			{1,0,0,0,0,0,1,1},
							 			{0,0,0,0,0,0,1,0},
							 			{0,0,0,0,0,0,0,1},
							 			{0,0,0,0,0,0,0,0},
							 			{0,0,0,0,0,0,0,0},
							 			{1,1,1,0,0,0,0,1},
							 			{1,1,0,1,0,0,1,0}};

			//Set up window attributes
			setTitle("File Viewer");
			setSize(640,480);
			Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
			setLocation((int)d.getWidth()/2 - 320, (int)d.getHeight()/2 - 240);
			setResizable(false);

			filePanel = new JPanel(new BorderLayout());
			welcomePanel = new JPanel(new GridLayout(3,1));
			deetsPanel = new JPanel(new GridLayout(2,1));

			//Set up details panel
			fileDetails = new JPanel(new GridLayout(3,2));
			openPanel = new JPanel();
			open = new JButton("Open");
			open.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//Obtain file "string" from server and open it in a new window with text area.

					try {
						openFile = selectedFile;
						//Step 1: Request file from server

						writer.write("open" + (char)13 + selectedFile + (char)13);
						writer.flush();

						//Step 2: Receive file; 0 at end

						int i = rdr.read();
						StringBuilder sb2 = new StringBuilder();
						while (i != 0) {
							sb2.append((char)i);
							i = rdr.read();
						}
						//3) File obtained. Now view it.
						JFrame frame = new JFrame(selectedFile);
						String text = sb2.toString();
						JTextArea ta = new JTextArea();
						int charIndex = 0,
							textLen = text.length();
						while (charIndex < textLen) {
							char c = text.charAt(charIndex);
							if (c == (char)13) {
								ta.append('\n' + "");
								charIndex++;
							}
							else
								ta.append(c + "");
							charIndex++;
						}

						ta.setLineWrap(true);

						//Check write permissions for this thing
						int index2 = -1;
						for (int k = 0; k < fileList.length; k++){
							if (selectedFile.equals(fileList[k])){
								index2 = k;
								break;
							}
						}
						ta.setEditable((write[index2] ? true : false));
						frame.add(ta);
						frame.setSize(640,480);

						if (write[index2])
							frame.addWindowListener(new CloseListenerPlus());
						frame.setVisible(true);

					}catch (IOException x) {
						JOptionPane.showMessageDialog(null, "Disconnected from server. Please refer to your system administrator "+
													  "for further assistance.");
						System.exit(0);
					}
				}
			});

			openPanel.add(open);

			nameLabel = new JLabel(fileList[0]);
			dateCLabel = new JLabel("Date created: " + datesCreated[0]);
			dateMLabel = new JLabel("Last modified: " + datesModified[0]);
			authLabel = new JLabel("Author: " + authorList[0]);
			sizeLabel = new JLabel("Size: " + sizes[0] + " KB");
			nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
			dateCLabel.setHorizontalAlignment(SwingConstants.CENTER);
			dateMLabel.setHorizontalAlignment(SwingConstants.CENTER);
			authLabel.setHorizontalAlignment(SwingConstants.CENTER);
			sizeLabel.setHorizontalAlignment(SwingConstants.CENTER);

			fileDetails.add(nameLabel);
			fileDetails.add(new JPanel());
			fileDetails.add(authLabel);
			fileDetails.add(dateCLabel);
			fileDetails.add(sizeLabel);
			fileDetails.add(dateMLabel);

			//Set up file panel
			JLabel fycr = new JLabel("Hello, " + username + "! Here are the files you may read:");
			fycr.setHorizontalAlignment(SwingConstants.CENTER);
			fycr.setPreferredSize(new Dimension(640,35));
			add(fycr, BorderLayout.NORTH);
			list = new JList<String>(fileList);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.addListSelectionListener(new ListSelectionListener(){
				public void valueChanged(ListSelectionEvent e) {
					selectedFile = (String)(((JList)(e.getSource())).getSelectedValue());
					if (selectedFile == null)
						selectedFile = "";
					if (!selectedFile.equals("No files accessible!"))
						updateLabels(selectedFile);
				}
			});

			list.setSelectedIndex(0);
			JScrollPane sp = new JScrollPane(list);
			filePanel.add(sp,BorderLayout.CENTER);

			//Set up welcome panel
			int i = 0;
			while (i < 8) {
				if (currentRole.equals(roles[i]))
					break;
				i++;
			}

			JLabel hello = new JLabel("<html><div style=\"text-align: center;\">You are currently in a session with the following roles:</html>");
			roleLabel = new JLabel(roles[i]);
			hello.setHorizontalAlignment(SwingConstants.CENTER);
			hello.setVerticalAlignment(SwingConstants.CENTER);
			roleLabel.setHorizontalAlignment(SwingConstants.CENTER);
			roleLabel.setVerticalAlignment(SwingConstants.CENTER);

			welcomePanel.add(hello);
			welcomePanel.add(roleLabel);

			JPanel buttons = new JPanel();
			JLabel rolelabel = new JLabel("Add role:");
			JComboBox roleBox = new JComboBox(roleList);
			roleBox.addActionListener(new ComboListener());
			JButton	quit = new JButton("Quit");
			quit.addActionListener(new CloseListener());
			buttons.add(rolelabel);
			buttons.add(roleBox);
			buttons.add(quit);
			welcomePanel.add(buttons);

			deetsPanel.add(fileDetails);
			deetsPanel.add(openPanel);

			JPanel lower = new JPanel(new GridLayout(1,2));
			lower.add(deetsPanel);
			lower.add(welcomePanel);

			//Add other empty panels for a nicer-looking GUI
			JPanel filler1 = new JPanel(),
				   filler2 = new JPanel();
			filler1.setPreferredSize(new Dimension(75, (int)filler1.getPreferredSize().getHeight()));
			filler2.setPreferredSize(new Dimension(75, (int)filler2.getPreferredSize().getHeight()));

			add(lower, BorderLayout.SOUTH);
			add(filler1, BorderLayout.WEST);
			add(filler2, BorderLayout.EAST);
			add(filePanel, BorderLayout.CENTER);

			addWindowListener(new CloseListener());
			setVisible(true);
		}

		//Will enable the "Open" button if a legitimate file is selected.
		private void updateOpenButton() {
			open.setEnabled((selectedFile == null || selectedFile.equals("")) ? false : true);
		}

		//When a user changes roles, this method changes the label displaying his/her active roles.
		private void updateRoleLabel() {
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (int i = 0; i < selectedRoles.length; i++) {
				if (selectedRoles[i] != null) {
					if (first)
						first = false;
					else
						sb.append(", ");
					sb.append(selectedRoles[i]);
				}
			}
			roleLabel.setText(sb.toString());
		}

		//Updates the file detail labels when a new file is selected.
		private void updateLabels(String selectedFile) {
			updateOpenButton();
			if (selectedFile == null) selectedFile = "";
			//Find the index of this file
			int index = -1;
			for (int i = 0; i < fileList.length; i++) {
				if (selectedFile.equals(fileList[i])){
					index = i;
					break;
				}
			} if (!selectedFile.equals("")) {
				nameLabel.setText(fileList[index]);
				dateCLabel.setText("Date created: " + datesCreated[index]);
				dateMLabel.setText("Last modified: " + datesModified[index]);
				authLabel.setText("Author: " + authorList[index]);
				sizeLabel.setText("Size: " + sizes[index] + " KB");
			}
			else {
				nameLabel.setText("");
				dateCLabel.setText("");
				dateMLabel.setText("");
				authLabel.setText("");
				sizeLabel.setText("");
			}
		}

		//Controls role selection. This implements dynamic SoD constraints through preventing conflicting roles from being
		// activated concurrently, but still allowing them to be active separately.
		private class ComboListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				String role = (String)(((JComboBox)e.getSource()).getSelectedItem());
				boolean activated = false;

				//Check if role is already activated.
				for (int i = 0; i < selectedRoles.length; i++) {
					if (role.equals(selectedRoles[i])) {
						//Update role label and selectedRoles[]. Update accessible files later.
						selectedRoles[i] = null;
						updateRoleLabel();
						activated = true;
						break;
					}
				}

				boolean conflict = false;
				if (!activated) {
					//Check for conflicting role. If conflicting, print error message and quit.
					int index = 0;
					while (!roles[index].equals(role))
						index++;	//found index of role
					int[] rcRow = roleConflicts[index];
					for (int i = 0; i < 8; i++) {
						if (rcRow[i] == 1) {
							//Check if this conflicting role is activated.
							for (int j = 0; j < selectedRoles.length; j++) {
								if (roles[i].equals(selectedRoles[j])) {
									//Output error message and return.
									JOptionPane.showMessageDialog(null, "Error: Cannot activate role due to conflicts.");
									conflict = true;
								}
							}
						}
					}
					if (!conflict) {
						for (int i = 0; i < selectedRoles.length; i++) {
							if (selectedRoles[i] == null) {
								selectedRoles[i] = role;
								updateRoleLabel();
								break;
							}
						}
					}
				}
				//Check if no active roles
				boolean activeRoles = false;
				for (int i = 0; i < selectedRoles.length; i++) {
					if (selectedRoles[i] != null){
						activeRoles = true;
						break;
					}
				}
				if (!activeRoles) {
					String[] s = {"No files accessible!"};
					list.setListData(s);
					updateLabels("");
					return;
				}

				//At this point, send request to get all readable files from server.
				if (!conflict) try {
					//Write roles
					writer.write("getfiles" + (char)13);
					writer.flush();
					for (int k = 0; k < selectedRoles.length; k++) {
						if (selectedRoles[k] != null) {
							writer.write(selectedRoles[k] + (char)13);
							writer.flush();
						}
					}
					writer.write("end" + (char)13);
					writer.flush();

					//First get the number of files.
					String in = FSUtility.readConnectionInputToken(rdr);
					int numOfFiles = Integer.parseInt(in);

					//Next get file details.
					fileList = new String[numOfFiles];
					datesCreated = new String[numOfFiles];
					datesModified = new String[numOfFiles];
					authorList = new String[numOfFiles];
					sizes = new int[numOfFiles];
					write = new boolean[numOfFiles];

					for (int k = 0; k < numOfFiles; k++) {
						fileList[k] = FSUtility.readConnectionInputToken(rdr);
						datesCreated[k] = FSUtility.readConnectionInputToken(rdr);
						datesModified[k] = FSUtility.readConnectionInputToken(rdr);
						authorList[k] = FSUtility.readConnectionInputToken(rdr);
						sizes[k] = Integer.parseInt(FSUtility.readConnectionInputToken(rdr));
						System.out.println(fileList[k]);
						System.out.println(datesCreated[k]);
						System.out.println(datesModified[k]);
						System.out.println(authorList[k]);
						System.out.println(sizes[k]);

						// Get write permission
						int i = rdr.read();
						write[k] = ((i == 0) ? false : true);
						System.out.println(write[k]);
						System.out.println();
						i = rdr.read();
					}
					list.setListData(fileList);
					updateOpenButton();
					//updateLabels("");


				}catch (IOException x) { System.out.println("Broke again..."); }

			}
		}
	}

	//Close program if red-X or Close button is clicked.
	private class CloseListener extends WindowAdapter implements ActionListener {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}

	//Asks the user if he/she wants to save his/her work on closing a writable file.
	private class CloseListenerPlus extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			int k = -1;
			while (k == -1) {
				k = JOptionPane.showConfirmDialog(null, "Would you like to save your changes?");
			}

			//If k = 0, save changes and exit. If k = 1, don't save but exit. If k = 2, do nothing.
			// (From dialog above: 0 = "Yes", 1 = "No", 2 = "Cancel")
			if (k == 2)
				((JFrame)e.getWindow()).setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

			else {
				((JFrame)e.getWindow()).setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

				//Save info if k = 0
				if (k == 0) {
					try {
						//Send "save" command to server
						writer.write("save" + (char)13 + openFile + (char)13);
						writer.flush();

						//Send new file.
						String text = (((JTextArea)((JFrame)e.getWindow()).getContentPane().getComponent(0))).getText();
						writer.write(text + (char)0);
						writer.flush();
					}catch (IOException x) {
						System.out.println("Error: Changes not saved.");
					}
				}
			}

		}
	}
}