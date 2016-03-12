package client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

public class FileViewer extends JFrame {

	private static final int WIDTH = 400,
							 HEIGHT = 300;

	private JTextArea textArea;
	private boolean editable;
	private String originalContents, clipboard;

	static FileViewer make(boolean editable, String fileContents) {
		return new FileViewer(editable, fileContents);
	}

	private FileViewer(boolean editable, String fileContents) {
		setTitle("File Viewer");
		setSize(WIDTH, HEIGHT);
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int)(screenDim.getWidth() / 4.0), (int)(screenDim.getHeight() / 4.0));
		setDefaultCloseOperation(HIDE_ON_CLOSE);

		this.editable = editable;
		originalContents = fileContents;

		textArea = initializeTextArea();
		add(textArea, BorderLayout.CENTER);

		JMenuBar menuBar = makeMenuBar();
		add(menuBar, BorderLayout.NORTH);
	}

	private JMenuBar makeMenuBar() {
		JMenu fileMenu = makeFileMenu();
		JMenu searchMenu = makeSearchMenu();

		JMenuBar menuBar = new JMenuBar();
		addItemsToMenuBar(menuBar, fileMenu);
		
		if (editable) {
			JMenu editMenu = makeEditMenu();
			addItemsToMenuBar(menuBar, editMenu);
		}
		addItemsToMenuBar(menuBar, searchMenu);
		return menuBar;
	}

	private JTextArea initializeTextArea() {
		JTextArea textArea = new JTextArea(originalContents);
		textArea.setLineWrap(true);
		textArea.setEditable(editable);
		return textArea;
	}

	private JMenu makeFileMenu() {
		JMenu fileMenu = new JMenu("File");
		if (editable) {
			JMenuItem save = new JMenuItem("Save");
			save.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
			save.addActionListener(new SaveListener());
			addItemsToMenu(fileMenu, save);
		}
		JMenuItem exit = new JMenuItem("Exit");
		exit.setAccelerator(KeyStroke.getKeyStroke("ctrl W"));
		exit.addActionListener(new ExitListener());
		
		addItemsToMenu(fileMenu, exit);
		return fileMenu;
	}

	private JMenu makeSearchMenu() {
		JMenu searchMenu = new JMenu("Search");
		JMenuItem find = new JMenuItem("Find"),
				  findRegex = new JMenuItem("Find Regular Expression");

		find.setAccelerator(KeyStroke.getKeyStroke("ctrl F"));
		findRegex.setAccelerator(KeyStroke.getKeyStroke("ctrl shift F"));
		find.addActionListener(new FindListener(false));
		findRegex.addActionListener(new FindListener(true));
		addItemsToMenu(searchMenu, find, findRegex);

		if (editable) {
			JMenuItem replace = new JMenuItem("Replace"),
					  replaceRegex = new JMenuItem("Replace Regular Exp.");
			replace.setAccelerator(KeyStroke.getKeyStroke("ctrl H"));
			replaceRegex.setAccelerator(KeyStroke.getKeyStroke("ctrl shift H"));
			replace.addActionListener(new ReplaceListener(false));
			replaceRegex.addActionListener(new ReplaceListener(true));
			addItemsToMenu(searchMenu, replace, replaceRegex);
		}
		return searchMenu;
	}

	private JMenu makeEditMenu() {
		JMenu editMenu = new JMenu("Edit");
		JMenuItem undo = new JMenuItem("Undo"),
				  redo = new JMenuItem("Redo"),
				  cut = new JMenuItem("Cut"),
				  copy = new JMenuItem("Copy"),
				  paste = new JMenuItem("Paste");

		undo.setAccelerator(KeyStroke.getKeyStroke("ctrl Z"));
		redo.setAccelerator(KeyStroke.getKeyStroke("ctrl Y"));
		cut.setAccelerator(KeyStroke.getKeyStroke("ctrl X"));
		copy.setAccelerator(KeyStroke.getKeyStroke("ctrl C"));
		paste.setAccelerator(KeyStroke.getKeyStroke("ctrl V"));

		undo.addActionListener(new UndoListener());
		redo.addActionListener(new RedoListener());
		cut.addActionListener(new CopyListener(true));
		copy.addActionListener(new CopyListener(false));
		paste.addActionListener(new PasteListener());

		addItemsToMenu(editMenu, undo, redo, cut, copy, paste);
		return editMenu;
	}

	private static void addItemsToMenuBar(JMenuBar menuBar, JMenu... menus) {
		for (JMenu menu : menus)
			menuBar.add(menu);
	}

	private static void addItemsToMenu(JMenu menu, JMenuItem... items) {
		for (JMenuItem item : items)
			menu.add(item);
	}

	private class SaveListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
		}
	}

	private class ExitListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
		}
	}

	private class UndoListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
		}
	}

	private class RedoListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
		}
	}

	private class CopyListener implements ActionListener {

		private boolean cut;

		CopyListener(boolean cut) {
			this.cut = cut;
		}

		public void actionPerformed(ActionEvent e) {
		}
	}

	private class PasteListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
		}
	}

	private class FindListener implements ActionListener {

		private boolean searchAsRegex;

		FindListener(boolean searchAsRegex) {
			this.searchAsRegex = searchAsRegex;
		}

		public void actionPerformed(ActionEvent e) {
		}
	}

	private class ReplaceListener implements ActionListener {

		private boolean replaceAsRegex;

		ReplaceListener(boolean replaceAsRegex) {
			this.replaceAsRegex = replaceAsRegex;
		}

		public void actionPerformed(ActionEvent e) {
		}
	}
}