package client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.LinkedList;
import java.util.function.*;
import javax.swing.*;
import javax.swing.event.*;

class FileMenu extends JFrame {
	
	private static final int WIDTH = 600,
							 HEIGHT = 450;

	private LinkedList<String> roles, selectedRoles;
	private JScrollPane fileListPane;
	private JLabel nameLabel, authLabel, sizeLabel, dateMadeLabel, dateModdedLabel, rolesLabel;
	private JButton openBtn;

	private FileMenu(LinkedList<String> roles, Runnable logoutMethod, 
						 Consumer<String> openMethod, Function<LinkedList<String>, String> fileInfoMethod) {
		setTitle("File Menu");
		setSize(WIDTH, HEIGHT);
		setResizable(false);
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int)(screenDim.getWidth() / 2.0 - WIDTH / 2), (int)(screenDim.getHeight() / 2.0 - HEIGHT / 2));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		selectedRoles = new LinkedList<>();

		add(makeTopPanel(logoutMethod), BorderLayout.NORTH);
		add(makeFileListPanel(openMethod), BorderLayout.CENTER);
		add(makeBottomPanel(roles, openMethod, fileInfoMethod), BorderLayout.SOUTH);
	}

	static FileMenu make(LinkedList<String> roles, Runnable logoutMethod, 
						 Consumer<String> openMethod, Function<LinkedList<String>, String> fileInfoMethod) {
		return new FileMenu(roles, logoutMethod, openMethod, fileInfoMethod);
	}

	private JPanel makeTopPanel(Runnable logoutMethod) {
		JLabel topLabel = new JLabel("Available files:\t\t\t");
		Font labelFont = topLabel.getFont();
		topLabel.setFont(new Font(
							labelFont.getName(),
							Font.BOLD,
							labelFont.getSize()
						));

		JButton logoutButton = new JButton("Logout");
		logoutButton.addActionListener(new LogoutListener(logoutMethod));

		JPanel panel = new JPanel();
		panel.add(topLabel);
		panel.add(logoutButton);
		return panel;
	}

	private JPanel makeFileListPanel(Consumer<String> openMethod) {
		String[] messageArr = {"No files here (yet)! Select a role to get started!"};
		JList<String> fileList = new JList<>(messageArr);
		fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fileList.addListSelectionListener(new FileSelectionListener());
		fileList.addMouseListener(new FileClickListener(openMethod));
		fileList.setEnabled(false);

		fileListPane = new JScrollPane(fileList);
		
		JPanel panel = new JPanel();
		panel.add(fileListPane);
		return panel;
	}

	private JPanel makeBottomPanel(LinkedList<String> roles, Consumer<String> openMethod, 
								     Function<LinkedList<String>, String> fileInfoMethod) {
		JPanel bottomPanel = new JPanel(new GridLayout(1,2));
		bottomPanel.add(makeDetailsBox(openMethod));
		bottomPanel.add(makeRolesBox(roles, fileInfoMethod));
		return bottomPanel;
	}

	private Box makeDetailsBox(Consumer<String> openMethod) {
		Box detailsBox = new Box(BoxLayout.Y_AXIS);
		detailsBox.add(makeFileNamePanel(openMethod));
		detailsBox.add(makeDetailsPanel());
		return detailsBox;
	}

	private JPanel makeFileNamePanel(Consumer<String> openMethod) {
		openBtn = new JButton("Open");
		openBtn.setEnabled(false);
		openBtn.addActionListener(new OpenButtonListener(openMethod));
		nameLabel = new JLabel("");

		JPanel namePanel = new JPanel();
		namePanel.add(openBtn);
		namePanel.add(nameLabel);
		return namePanel;
	}

	private JPanel makeDetailsPanel() {
		authLabel = new JLabel("<Author>");
		sizeLabel = new JLabel("<Size>");
		dateMadeLabel = new JLabel("<Date created>");
		dateModdedLabel = new JLabel("<Date modified>");
		
		JPanel detailsPanel = new JPanel(new GridLayout(2,2));
		detailsPanel.add(authLabel);
		detailsPanel.add(sizeLabel);
		detailsPanel.add(dateMadeLabel);
		detailsPanel.add(dateModdedLabel);
		detailsPanel.setBorder(BorderFactory.createTitledBorder("File Details"));

		return detailsPanel;
	}

	private Box makeRolesBox(LinkedList<String> roles, Function<LinkedList<String>, String> fileInfoMethod) {
		Box rolesBox = new Box(BoxLayout.Y_AXIS);
		rolesBox.setBorder(BorderFactory.createTitledBorder("Roles"));

		rolesBox.add(makeRoleHeaderPanel());
		rolesBox.add(makeRolesPanel());
		rolesBox.add(makeRoleAdderPanel(roles, fileInfoMethod));

		return rolesBox;
	}

	private JPanel makeRoleHeaderPanel() {
		JLabel header = new JLabel("You are currently in a session with the following roles:");
		JPanel panel = new JPanel();
		panel.add(header);
		return panel;
	}

	private JPanel makeRolesPanel() {
		rolesLabel = new JLabel("(No roles selected.)");
		JPanel panel = new JPanel();
		panel.add(rolesLabel);
		return panel;
	}

	private JPanel makeRoleAdderPanel(LinkedList<String> roles, Function<LinkedList<String>, String> fileInfoMethod) {
		JLabel addLabel = new JLabel("Add/delete role:");
		String[] roleArr = listToArray(roles);
		JComboBox<String> roleBox = new JComboBox<>(roleArr);
		roleBox.addActionListener(new ComboBoxListener(fileInfoMethod));

		JPanel panel = new JPanel();
		panel.add(addLabel);
		panel.add(roleBox);
		return panel;
	}

	private static String[] listToArray(LinkedList<String> list) {
		String[] arr = new String[list.size()];
		int i = 0;
		for (String s : list) {
			arr[i] = s;
			i++;
		}
		return arr;
	}

	private static class LogoutListener implements ActionListener {

		private Runnable logoutMethod;

		LogoutListener(Runnable logoutMethod) {
			this.logoutMethod = logoutMethod;
		}

		public void actionPerformed(ActionEvent e) {
			// similar ribble
		}
	}

	private static class ComboBoxListener implements ActionListener {

		private Function<LinkedList<String>, String> fileInfoMethod;

		ComboBoxListener(Function<LinkedList<String>, String> fileInfoMethod) {
			this.fileInfoMethod = fileInfoMethod;
		}
		
		public void actionPerformed(ActionEvent e) {
			// similar ribble
		}
	}

	private static class OpenButtonListener implements ActionListener {

		private Consumer<String> openMethod;

		OpenButtonListener(Consumer<String> openMethod) {
			this.openMethod = openMethod;
		}

		public void actionPerformed(ActionEvent e) {
			// similar ribble
		}
	}

	private static class FileClickListener extends MouseAdapter {

		private Consumer<String> openMethod;

		FileClickListener(Consumer<String> openMethod) {
			this.openMethod = openMethod;
		}

		public void mouseClicked(MouseEvent e) {
			JList list = (JList)e.getSource();
			if (e.getClickCount() >= 2) {
				// double click
			}
			// other ideas?
		}
	}

	private static class FileSelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			// ribble
		}
	}
}