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

	static FileMenu make(LinkedList<String> roles, Runnable logoutMethod,
							FOConsumer openMethod, FIFunction fileInfoMethod) {

		return new FileMenu(roles, logoutMethod, openMethod, fileInfoMethod);
	}

	private FileMenu(LinkedList<String> roles, Runnable logoutMethod,
						FOConsumer openMethod, FIFunction fileInfoMethod) {

		setTitle("File Menu");
		setSize(WIDTH, HEIGHT);
		setResizable(false);
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int)(screenDim.getWidth() / 2.0 - WIDTH / 2), (int)(screenDim.getHeight() / 2.0 - HEIGHT / 2));
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		JPanel topPanel = makeTopPanel(logoutMethod);
		add(topPanel, BorderLayout.NORTH);

		JList<RBFSFile> fileList = initFileList(openMethod);
		JPanel fileListPanel = makeFileListPanel(fileList);
		add(fileListPanel, BorderLayout.CENTER);

		Box detailsBox = makeDetailsBox(fileList, openMethod),
			rolesBox = makeRolesBox(roles);

		fileList.addListSelectionListener(new ListSelectionListener() {
											public void valueChanged(ListSelectionEvent e) {

											}
										  });

		JPanel bottomPanel = makeBottomPanel(detailsBox, rolesBox);
		add(bottomPanel, BorderLayout.SOUTH);
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

	private static JList<RBFSFile> initFileList(FOConsumer openMethod) {
		JList<RBFSFile> fileList = new JList<>();
		fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fileList.addMouseListener(new OpenListener(fileList, openMethod));
		return fileList;
	}

	private static JPanel makeFileListPanel(JList<RBFSFile> fileList) {
		JScrollPane fileListScrollPane = new JScrollPane(fileList);
		JPanel fileListPanel = new JPanel();
		fileListPanel.add(fileListScrollPane);
		return fileListPanel;
	}

	private Box makeDetailsBox(JList<RBFSFile> fileList, FOConsumer openMethod) {
		JLabel nameLabel = new JLabel(""),
			   authLabel = new JLabel("<Author>"),
			   sizeLabel = new JLabel("<Size>"),
			   dateMadeLabel = new JLabel("<Date created>"),
			   dateModdedLabel = new JLabel("<Date modified>");
		labels = new LabelSet(nameLabel, authLabel, sizeLabel, sizeLabel, dateMadeLabel, dateModdedLabel);

		JPanel upperPanel = makeUpperDetailsPanel(fileList, openMethod, nameLabel),
			   lowerPanel = makeLowerDetailsPanel(authLabel, sizeLabel, dateMadeLabel, dateModdedLabel);
		
		Box detailsBox = new Box(BoxLayout.Y_AXIS);
		detailsBox.add(upperPanel);
		detailsBox.add(lowerPanel);
		return detailsBox;
	}

	private static JPanel makeUpperDetailsPanel(JList<RBFSFile> fileList, FOConsumer openMethod, JLabel nameLabel) {
		JButton openBtn = new JButton("Open");
		openBtn.addActionListener(new OpenListener(fileList, openMethod));

		JPanel upperPanel = new JPanel();
		upperPanel.add(openBtn);
		upperPanel.add(nameLabel);
		return upperPanel;
	}

	private static JPanel makeLowerDetailsPanel(JLabel authLabel, JLabel sizeLabel, 
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

	private static JPanel makeHeaderPanel() {
		JLabel header = new JLabel("You are currently in a session with the following roles:");
		JPanel headerPanel = new JPanel();
		headerPanel.add(header);
		return headerPanel;
	}

	private static JPanel makeRoleDisplayPanel(JLabel rolesLabel) {
		JPanel rolePanel = new JPanel();
		rolePanel.add(rolesLabel);
		return rolePanel;
	}

	private static JPanel makeRoleAdderPanel(LinkedList<String> roles) {
		JLabel addLabel = new JLabel("Add/delete role:");
		String[] roleArr = listToArray(roles);
		JComboBox<String> roleComboBox = new JComboBox<>(roleArr);
		roleComboBox.addActionListener(new ActionListener() {
											public void actionPerformed(ActionEvent e) {
											}
								  		});

		JPanel adderPanel = new JPanel();
		adderPanel.add(addLabel);
		adderPanel.add(roleComboBox);
		return adderPanel;
	}

	private static JPanel makeBottomPanel(Box detailsBox, Box rolesBox) {
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

	private static class OpenListener extends MouseAdapter
									  implements ActionListener {

		private JList<RBFSFile> fileList;
		private FOConsumer openMethod;

		OpenListener(JList<RBFSFile> fileList, FOConsumer openMethod) {
			this.fileList = fileList;
			this.openMethod = openMethod;
		}

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
					fileList.setListData(listToArray(folder.getChildFiles()));
				}
			}
		}
	}
}