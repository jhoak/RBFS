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

	private JList<String> fileList;
	private LabelSet labels;
	private JLabel nameLabel, authLabel, sizeLabel, dateMadeLabel, dateModdedLabel, rolesLabel;
	private JButton openBtn;

	private FileMenu(LinkedList<String> roles, Runnable logoutMethod, 
					 FOConsumer openMethod, FIFunction fileInfoMethod) {
		setTitle("File Menu");
		setSize(WIDTH, HEIGHT);
		setResizable(false);
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int)(screenDim.getWidth() / 2.0 - WIDTH / 2), (int)(screenDim.getHeight() / 2.0 - HEIGHT / 2));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		add(makeTopPanel(logoutMethod), BorderLayout.NORTH);
		add(makeFileListPanel(openMethod), BorderLayout.CENTER);
		add(makeBottomPanel(roles, openMethod, fileInfoMethod), BorderLayout.SOUTH);
	}

	static FileMenu make(LinkedList<String> roles, Runnable logoutMethod, 
						 FOConsumer openMethod, FIFunction fileInfoMethod) {
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
		return addComponents(panel, topLabel, logoutButton);
	}

	private JPanel makeFileListPanel(FOConsumer openMethod) {
		String[] messageArr = {"No files here (yet)! Select a role to get started!"};
		fileList = new JList<>(messageArr);
		fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fileList.addListSelectionListener(new FileSelectionListener());
		fileList.addMouseListener(new OpenListener(openMethod, fileList));
		fileList.setEnabled(false);

		JScrollPane fileListPane = new JScrollPane(fileList);
		return addComponents(new JPanel(), fileListPane);
	}

	private JPanel makeBottomPanel(LinkedList<String> roles, FOConsumer openMethod, 
								   FIFunction fileInfoMethod) {
		return addComponents(
			new JPanel(new GridLayout(1,2)),
			makeDetailsBox(openMethod),
			makeRolesBox(roles, fileInfoMethod)
		);
	}

	private Box makeDetailsBox(FOConsumer openMethod) {
		return addComponents(
			new Box(BoxLayout.Y_AXIS),
			makeFileNamePanel(openMethod),
			makeDetailsPanel()
		);
	}

	private JPanel makeFileNamePanel(FOConsumer openMethod) {
		openBtn = new JButton("Open");
		openBtn.setEnabled(false);
		openBtn.addActionListener(new OpenListener(openMethod, fileList));
		nameLabel = new JLabel("");

		return addComponents(new JPanel(), openBtn, nameLabel);
	}

	private JPanel makeDetailsPanel() {
		authLabel = new JLabel("<Author>");
		sizeLabel = new JLabel("<Size>");
		dateMadeLabel = new JLabel("<Date created>");
		dateModdedLabel = new JLabel("<Date modified>");
		
		JPanel detailsPanel = addComponents(
			new JPanel(new GridLayout(2,2)),
			authLabel,
			sizeLabel,
			dateMadeLabel,
			dateModdedLabel
		);
		detailsPanel.setBorder(BorderFactory.createTitledBorder("File Details"));
		return detailsPanel;
	}

	private Box makeRolesBox(LinkedList<String> roles, FIFunction fileInfoMethod) {
		Box rolesBox = new Box(BoxLayout.Y_AXIS);
		rolesBox.setBorder(BorderFactory.createTitledBorder("Roles"));

		return addComponents(
			rolesBox, 
			makeRoleHeaderPanel(), 
			makeRolesPanel(), 
			makeRoleAdderPanel(roles, fileInfoMethod)
		);
	}

	private JPanel makeRoleHeaderPanel() {
		JLabel header = new JLabel("You are currently in a session with the following roles:");
		return addComponents(new JPanel(), header);
	}

	private JPanel makeRolesPanel() {
		rolesLabel = new JLabel("(No roles selected.)");
		return addComponents(new JPanel(), rolesLabel);
	}

	private JPanel makeRoleAdderPanel(LinkedList<String> roles, FIFunction fileInfoMethod) {
		JLabel addLabel = new JLabel("Add/delete role:");
		String[] roleArr = listToArray(roles);
		JComboBox<String> roleBox = new JComboBox<>(roleArr);
		roleBox.addActionListener(new ComboBoxListener(fileInfoMethod));

		return addComponents(new JPanel(), addLabel, roleBox);
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

	private static JPanel addComponents(JPanel panel, Component... comps) {
		for (Component c : comps)
			panel.add(c);
		return panel;
	}

	private static Box addComponents(Box box, Component... comps) {
		for (Component c : comps)
			box.add(c);
		return box;
	}

	private static void openFile(FOConsumer openMethod, JList<String> fileList) {
		String selectedFile = getSelectedFile(fileList);
		if (selectedFile != null)
			openMethod.accept(selectedFile);
	}

	private static String getSelectedFile(JList<String> fileList) {
		return fileList.getSelectedValue();
	}

	private static class LogoutListener implements ActionListener {

		private Runnable logoutMethod;

		LogoutListener(Runnable logoutMethod) {
			this.logoutMethod = logoutMethod;
		}

		public void actionPerformed(ActionEvent e) {
			logoutMethod.run();
		}
	}

	private static class ComboBoxListener implements ActionListener {

		private FIFunction fileInfoMethod;

		ComboBoxListener(FIFunction fileInfoMethod) {
			this.fileInfoMethod = fileInfoMethod;
		}

		public void actionPerformed(ActionEvent e) {
			// similar ribble
		}
	}

	private static class OpenListener extends MouseAdapter
									  implements ActionListener {

		private FOConsumer openMethod;
		private JList<String> fileList;

		OpenListener(FOConsumer openMethod, JList<String> fileList) {
			this.openMethod = openMethod;
			this.fileList = fileList;
		}

		public void actionPerformed(ActionEvent e) {
			openFile(openMethod, fileList);
		}

		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() >= 2) {
				openFile(openMethod, fileList);
			}
		}
	}

	private static class FileSelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			// ribble
		}
	}
}