package rbfs.client;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.function.*;
import javax.swing.*;
import javax.swing.event.*;
import rbfs.client.fcn.*;
import rbfs.client.util.*;
import rbfs.file.*;

/**
This window is the first screen users see upon connecting to a RBFS server.
Here, they can choose their active roles and navigate the server's file
hierarchy, choosing to read and edit files on the server (given that they have
the required permissions for reading/editing). File metadata is also shown on
this screen for convenience.

@author	James Hoak
*/

class FileMenu extends JFrame {

	private static final int WIDTH = 600,		// Window width
							 HEIGHT = 450;		// and height

	// GUI components
	private LabelSet labels;
	private JList<RBFSFile> fileList;
	
	// Functions(-al interfaces) for handling different events
	private Runnable logoutMethod;
	private FIFunction fileInfoMethod;
	private FOConsumer openMethod;

	/**
	Factory method for creating a new FileMenu with a set of roles to choose
	from, as well as methods for handling different actions the user wants to
	take.
	@param roles The list of all roles the user may activate
	@param logoutMethod The actions to take when the user wants to log out
	@param fileInfoMethod The actions to take when the user wants to see new file mtadata
	@param openMethod Action to perform when the user is opening a file
	@return A new FileMenu with the requested roles and functions.@
	*/
	static FileMenu make(LinkedList<String> roles, Runnable logoutMethod,
						 FIFunction fileInfoMethod, FOConsumer openMethod) {

		return new FileMenu(roles, logoutMethod, fileInfoMethod, openMethod);
	}

	/**
	Creates a new FileMenu, which allows the user to browse through all of the
	files they may access on the server.
	@param roles The list of all roles the user may activate
	@param logoutMethod The actions to take when the user wants to log out
	@param fileInfoMethod The actions to take when the user wants to see new file mtadata
	@param openMethod Action to perform when the user is opening a file
	*/
	private FileMenu(LinkedList<String> roles, Runnable logoutMethod,
					 FIFunction fileInfoMethod, FOConsumer openMethod) {
		
		this.logoutMethod = logoutMethod;
		this.fileInfoMethod = fileInfoMethod;
		this.openMethod = openMethod;

		// Window attribute setup
		setTitle("File Menu");
		setSize(WIDTH, HEIGHT);
		setResizable(false);
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int)(screenDim.getWidth() / 2.0 - WIDTH / 2), (int)(screenDim.getHeight() / 2.0 - HEIGHT / 2));
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// GUI components setup
		JPanel topPanel = makeTopPanel();
		add(topPanel, BorderLayout.NORTH);

		fileList = initFileList();
		JPanel fileListPanel = makeFileListPanel();
		add(fileListPanel, BorderLayout.CENTER);

		Box detailsBox = makeDetailsBox(),
			rolesBox = makeRolesBox(roles);

		// Allow the list to react when we pick a file (by editing the metadata
		// labels on the lower left details box).
		fileList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				RBFSFile file = fileList.getSelectedValue();
				if (file != null) {
					String name = file.getName();
					if (name.startsWith(".. ("))
						name = name.substring(4, name.length() - 1);
					labels.setText(
						name,
						"Author: " + file.getAuthor(),
						"Size: " + file.getSize(),
						"Date created: " + file.getDateMade(),
						"Date modified: " + file.getDateModded()
					);
				}
			}
		});

		JPanel bottomPanel = makeBottomPanel(detailsBox, rolesBox);
		add(bottomPanel, BorderLayout.SOUTH);
	}

	/**
	Make a header panel with some text and a logout button.
	@return A panel with an "Available files:" label on one side and a logout
	button on the other.
	*/
	private JPanel makeTopPanel() {
		// Make the label first, and make its text bold
		JLabel topLabel = new JLabel("Available files:\t\t\t");
		Font labelFont = topLabel.getFont();
		topLabel.setFont(new Font(
							labelFont.getName(),
							Font.BOLD,
							labelFont.getSize()
						));

		// Make the button next and tell it to run the logout method on click
		JButton logoutButton = new JButton("Logout");
		logoutButton.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											logoutMethod.run();
										}
									   });

		// Return a new panel with these components
		JPanel topPanel = new JPanel();
		topPanel.add(topLabel);
		topPanel.add(logoutButton);
		return topPanel;
	}

	/**
	Make a new empty list to list all of the files the user may access,
	and set its attributes appropriately.
	@return A JList to use for displaying the files.
	*/
	private JList<RBFSFile> initFileList() {
		JList<RBFSFile> fileList = new JList<>();
		fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fileList.addMouseListener(new OpenListener());
		return fileList;
	}

	/**
	Make a new panel to display the list of files we just made.
	@return A new panel that displays our previously-created JList's file names.
	*/
	private JPanel makeFileListPanel() {
		JScrollPane fileListScrollPane = new JScrollPane(fileList);
		JPanel fileListPanel = new JPanel();
		fileListPanel.add(fileListScrollPane);
		return fileListPanel;
	}

	/**
	Make a small component that displays the file metadata for the currently-
	selected file.
	@return A Box component that holds the file metadata.
	*/
	private Box makeDetailsBox() {
		JLabel nameLabel = new JLabel(""),
			   authLabel = new JLabel("<Author>"),
			   sizeLabel = new JLabel("<Size>"),
			   dateMadeLabel = new JLabel("<Date created>"),
			   dateModdedLabel = new JLabel("<Date modified>");

		labels = new LabelSet(nameLabel, authLabel, sizeLabel, sizeLabel, dateMadeLabel, dateModdedLabel);

		JPanel upperPanel = makeUpperDetailsPanel(nameLabel),
			   lowerPanel = makeLowerDetailsPanel(authLabel, sizeLabel, dateMadeLabel, dateModdedLabel);
		
		Box detailsBox = new Box(BoxLayout.Y_AXIS);
		detailsBox.add(upperPanel);
		detailsBox.add(lowerPanel);
		return detailsBox;
	}

	/**
	Make a panel that shows the file's name as well as a button that opens it.
	@param nameLabel The label that stores the name of the file.
	@return A new panel that shows the file's name and a button to open it.
	*/
	private JPanel makeUpperDetailsPanel(JLabel nameLabel) {
		JButton openBtn = new JButton("Open");
		openBtn.addActionListener(new OpenListener());

		JPanel upperPanel = new JPanel();
		upperPanel.add(openBtn);
		upperPanel.add(nameLabel);
		return upperPanel;
	}

	/**
	Make the panel that simply holds the metadata (not including the name).
	@param authLabel The author label
	@param sizeLabel The size label
	@param dateMadeLabel The label displaying the date of creation
	@param dateModdedLabel The label displaying the date of the last modification
	@return A new JPanel that holds the given metadata labels
	*/
	private JPanel makeLowerDetailsPanel(JLabel authLabel, JLabel sizeLabel, 
												JLabel dateMadeLabel, JLabel dateModdedLabel) {
		
		JPanel lowerPanel = new JPanel(new GridLayout(2,2));
		lowerPanel.add(authLabel);
		lowerPanel.add(sizeLabel);
		lowerPanel.add(dateMadeLabel);
		lowerPanel.add(dateModdedLabel);
		lowerPanel.setBorder(BorderFactory.createTitledBorder("File Details"));
		return lowerPanel;
	}

	/**
	Make and return the box that displays role information to the user.
	@param roles The list of roles that the user may activate
	@return A Box that holds role information for the user
	*/
	private Box makeRolesBox(LinkedList<String> roles) {
		JLabel rolesLabel = new JLabel("No roles selected. Select one to get started!");

		JPanel headerPanel = makeHeaderPanel(),
			   roleDisplayPanel = makeRoleDisplayPanel(rolesLabel),
			   roleAdderPanel = makeRoleAdderPanel(roles);

		Box rolesBox = new Box(BoxLayout.Y_AXIS);
		rolesBox.setBorder(BorderFactory.createTitledBorder("Roles"));
		rolesBox.add(headerPanel);
		rolesBox.add(roleDisplayPanel);
		rolesBox.add(roleAdderPanel);
		return rolesBox;
	}

	/**
	Make the header panel at the top of the role info box.
	@return Returns a JPanel informing the user of their session.
	*/
	private JPanel makeHeaderPanel() {
		JLabel header = new JLabel("You are currently in a session with the following roles:");
		JPanel headerPanel = new JPanel();
		headerPanel.add(header);
		return headerPanel;
	}

	/**
	Make the panel that displays all the activated roles.
	@param rolesLabel The label that shows the list of the user's activated roles
	@return A JPanel containing the list of roles as a comma-separated list
	*/
	private JPanel makeRoleDisplayPanel(JLabel rolesLabel) {
		JPanel rolePanel = new JPanel();
		rolePanel.add(rolesLabel);
		return rolePanel;
	}

	/**
	Creates a panel that allows the user to add or delete roles via a combo box.
	@param roles The list of roles the user may activate
	@return A panel with a combo box for (de)activating roles
	*/
	private JPanel makeRoleAdderPanel(LinkedList<String> roles) {
		JLabel addLabel = new JLabel("Add/delete role:");
		String[] roleArr = listToArray(roles);
		JComboBox<String> roleComboBox = new JComboBox<>(roleArr);
		
		// Use the combo box to change the user's roles and try to get the list
		// of available files from the server
		roleComboBox.addActionListener(new ActionListener() {

			private LinkedList<String> selectedRoles = new LinkedList<>();

			public void actionPerformed(ActionEvent e) {
				String role = (String)roleComboBox.getSelectedItem();

				// If we've already selected the role, remove it. Otherwise, add it.
				if (selectedRoles.contains(role))
					selectedRoles.remove(role);
				else
					selectedRoles.add(role);

				// Now use the list of active roles to get the viewable files
				// from the server. If we have an error, just show it to the
				// user. In the case of an I/O error, time to stop the session.
				try {
					String svrResponse = fileInfoMethod.accept(selectedRoles);
					RBFSFolder root = RBFSFolder.makeDirectoryTree(svrResponse);
					fileList.setListData(listToArray(root.getFiles()));
				}
				catch (BadPermissionsException x) {
					JOptionPane.showMessageDialog(
						null,
						x.getMessage()
					);
				}
				catch (IOException x) {
					JOptionPane.showMessageDialog(
						null,
						x.getMessage()
					);
					logoutMethod.run();
				}
			}
  		});

		// Add our components to a new panel and return it
		JPanel adderPanel = new JPanel();
		adderPanel.add(addLabel);
		adderPanel.add(roleComboBox);
		return adderPanel;
	}

	/**
	Creates the entire bottom part of the menu's window (including the file
	details panel and the role display).
	@param detailsBox The file metadata component
	@param rolesBox The roles box component
	@return A new JPanel with the given components added
	*/
	private JPanel makeBottomPanel(Box detailsBox, Box rolesBox) {
		JPanel bottomPanel = new JPanel(new GridLayout(1, 2));
		bottomPanel.add(detailsBox);
		bottomPanel.add(rolesBox);
		return bottomPanel;
	}
	
	/**
	Utility method that converts a LinkedList of one type to an array of that type.
	@param list The list to convert
	@param <E> The type of list to convert
	@return An array with all of the list's contents
	*/
	private static <E> E[] listToArray(LinkedList<E> list) {
		E[] arr = (E[])new Object[list.size()];
		int i = 0;
		for (E obj : list) {
			arr[i] = obj;
			i++;
		}
		return arr;
	}

	/**
	This listener handles opening a file, using an action given to it by the
	calling program ("Client.java"). It works via either a double-clicn in the
	file selection pane or a click on the Open button in the metadata panel.
	If we're opening a file, ask the server for it. Otherwise, open the folder
	and show its contents in the file selection pane.
	*/
	private class OpenListener extends MouseAdapter
							   implements ActionListener {

		/**
		Happens when the user clicks on a file in the selection pane. If it's a
		double-click at least, then we'll try to open the file.
		@param e The MouseEvent (click) that fired this listener
		*/
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() >= 2)
				actionPerformed(null);
		}

		/**
		Opens a file if we have the permissions and it's actually a file.
		Otherwise, if it's a folder, opens that and shows its contents.
		Shows an error if we don't have the right permissions.
		@param e The ActionEvent (open button click) that fired this listener
		*/
		public void actionPerformed(ActionEvent e) {
			RBFSFile selectedFile = fileList.getSelectedValue();
			if (selectedFile != null) {
				if (selectedFile.getStorageType() == StorageType.FILE) {
					
					// If it's a file, let's open it and see its contents.
					// On failure, show an error message.
					try {
						openMethod.accept(selectedFile.getName());
					}
					catch (BadPermissionsException x) {
						JOptionPane.showMessageDialog(null, x.getMessage());
					}
				}

				// If it's a folder, show the files/folders that are inside it.
				else {
					RBFSFolder folder = (RBFSFolder)selectedFile;
					fileList.setListData(listToArray(folder.getFiles()));
				}
			}
		}
	}
}
