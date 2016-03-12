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

	private LabelSet labels;
	private JList<RBFSFile> fileList;
	private Runnable logoutMethod;
	private FIFunction fileInfoMethod;
	private FOConsumer openMethod;

	static FileMenu make(LinkedList<String> roles, Runnable logoutMethod,
						 FIFunction fileInfoMethod, FOConsumer openMethod) {

		return new FileMenu(roles, logoutMethod, fileInfoMethod, openMethod);
	}

	private FileMenu(LinkedList<String> roles, Runnable logoutMethod,
					 FIFunction fileInfoMethod, FOConsumer openMethod) {
		
		this.logoutMethod = logoutMethod;
		this.fileInfoMethod = fileInfoMethod;
		this.openMethod = openMethod;

		setTitle("File Menu");
		setSize(WIDTH, HEIGHT);
		setResizable(false);
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int)(screenDim.getWidth() / 2.0 - WIDTH / 2), (int)(screenDim.getHeight() / 2.0 - HEIGHT / 2));
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		JPanel topPanel = makeTopPanel();
		add(topPanel, BorderLayout.NORTH);

		fileList = initFileList();
		JPanel fileListPanel = makeFileListPanel();
		add(fileListPanel, BorderLayout.CENTER);

		Box detailsBox = makeDetailsBox(),
			rolesBox = makeRolesBox(roles);

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

	private JPanel makeTopPanel() {
		JLabel topLabel = new JLabel("Available files:\t\t\t");
		Font labelFont = topLabel.getFont();
		topLabel.setFont(new Font(
							labelFont.getName(),
							Font.BOLD,
							labelFont.getSize()
						));

		JButton logoutButton = new JButton("Logout");
		logoutButton.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											logoutMethod.run();
										}
									   });
		JPanel topPanel = new JPanel();
		topPanel.add(topLabel);
		topPanel.add(logoutButton);
		return topPanel;
	}

	private JList<RBFSFile> initFileList() {
		JList<RBFSFile> fileList = new JList<>();
		fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fileList.addMouseListener(new OpenListener());
		return fileList;
	}

	private JPanel makeFileListPanel() {
		JScrollPane fileListScrollPane = new JScrollPane(fileList);
		JPanel fileListPanel = new JPanel();
		fileListPanel.add(fileListScrollPane);
		return fileListPanel;
	}

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

	private JPanel makeUpperDetailsPanel(JLabel nameLabel) {
		JButton openBtn = new JButton("Open");
		openBtn.addActionListener(new OpenListener());

		JPanel upperPanel = new JPanel();
		upperPanel.add(openBtn);
		upperPanel.add(nameLabel);
		return upperPanel;
	}

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

	private JPanel makeHeaderPanel() {
		JLabel header = new JLabel("You are currently in a session with the following roles:");
		JPanel headerPanel = new JPanel();
		headerPanel.add(header);
		return headerPanel;
	}

	private JPanel makeRoleDisplayPanel(JLabel rolesLabel) {
		JPanel rolePanel = new JPanel();
		rolePanel.add(rolesLabel);
		return rolePanel;
	}

	private JPanel makeRoleAdderPanel(LinkedList<String> roles) {
		JLabel addLabel = new JLabel("Add/delete role:");
		String[] roleArr = listToArray(roles);
		JComboBox<String> roleComboBox = new JComboBox<>(roleArr);
		roleComboBox.addActionListener(new ActionListener() {

											private LinkedList<String> selectedRoles = new LinkedList<>();

											public void actionPerformed(ActionEvent e) {
												JComboBox<String> box = (JComboBox<String>)e.getSource();
												String role = (String)box.getSelectedItem();
												if (selectedRoles.contains(role))
													selectedRoles.remove(role);
												else
													selectedRoles.add(role);
												try {
													String svrResponse = fileInfoMethod.accept(selectedRoles);
													RBFSFolder root = RBFSFolder.makeDirectoryTree(svrResponse);
													fileList.setListData(listToArray(root.getFiles()));
												}
												catch (Client.BadPermissionsException x) {
													JOptionPane.showMessageDialog(
														null,
														"Error: Inadequate permissions."
													);
												}
												catch (IOException x) {
													JOptionPane.showMessageDialog(
														null,
														"Error: Failed to communicate with server."
													);
													logoutMethod.run();
												}
											}
								  		});

		JPanel adderPanel = new JPanel();
		adderPanel.add(addLabel);
		adderPanel.add(roleComboBox);
		return adderPanel;
	}

	private JPanel makeBottomPanel(Box detailsBox, Box rolesBox) {
		JPanel bottomPanel = new JPanel(new GridLayout(1, 2));
		bottomPanel.add(detailsBox);
		bottomPanel.add(rolesBox);
		return bottomPanel;
	}
	
	private static <E> E[] listToArray(LinkedList<E> list) {
		E[] arr = (E[])new Object[list.size()];
		int i = 0;
		for (E obj : list) {
			arr[i] = obj;
			i++;
		}
		return arr;
	}

	private class OpenListener extends MouseAdapter
							   implements ActionListener {

		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() >= 2)
				actionPerformed(null);
		}

		public void actionPerformed(ActionEvent e) {
			RBFSFile selectedFile = fileList.getSelectedValue();
			if (selectedFile != null) {
				if (selectedFile.getStorageType() == StorageType.FILE)
					openMethod.accept(selectedFile.getName());
				else {
					RBFSFolder folder = (RBFSFolder)selectedFile;
					fileList.setListData(listToArray(folder.getFiles()));
				}
			}
		}
	}
}