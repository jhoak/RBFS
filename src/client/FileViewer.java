package client;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.event.*;

public class FileViewer extends JFrame {

	private static final int WIDTH = 400,
							 HEIGHT = 300;
	private static final String KEYSTRING = "Failed to paste text from RBFS file viewer!";

	private JTextArea textArea;
	private String clipboard;
	private boolean editable;

	static FileViewer make(boolean editable, String fileContents, FOConsumer saveFcn) {
		return new FileViewer(editable, fileContents, saveFcn);
	}

	private FileViewer(boolean editable, String fileContents, FOConsumer saveFcn) {
		setTitle("File Viewer");
		setSize(WIDTH, HEIGHT);
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int)(screenDim.getWidth() / 4.0), (int)(screenDim.getHeight() / 4.0));
		setDefaultCloseOperation(HIDE_ON_CLOSE);

		this.editable = editable;

		textArea = initializeTextArea(fileContents);
		JScrollPane scrollPane = new JScrollPane(textArea);
		add(scrollPane, BorderLayout.CENTER);

		JMenuBar menuBar = makeMenuBar(saveFcn);
		add(menuBar, BorderLayout.NORTH);
	}

	private JMenuBar makeMenuBar(FOConsumer saveFcn) {
		JMenu fileMenu = makeFileMenu(saveFcn);
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

	private JTextArea initializeTextArea(String fileContents) {
		JTextArea textArea = new JTextArea(fileContents);
		textArea.setLineWrap(true);
		textArea.setEditable(editable);
		textArea.getDocument().addDocumentListener(new TextChangedListener());
		return textArea;
	}

	private JMenu makeFileMenu(FOConsumer saveFcn) {
		JMenu fileMenu = new JMenu("File");
		if (editable) {
			JMenuItem save = new JMenuItem("Save");
			save.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
			save.addActionListener(new SaveListener(saveFcn));
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

	private class TextChangedListener implements DocumentListener {
    
    	public void insertUpdate(DocumentEvent e) {
        	removeUpdate(e);
    	}
    	
    	public void removeUpdate(DocumentEvent e) {
    		setTitle(getTitle() + "*");
    	}
    	
    	public void changedUpdate(DocumentEvent e) {
        	// Not used because plaintext
    	}
	}

	private class SaveListener implements ActionListener {

		private FOConsumer saveFcn;

		SaveListener(FOConsumer saveFcn) {
			this.saveFcn = saveFcn;
		}

		public void actionPerformed(ActionEvent e) {
			try {
				saveFcn.accept(textArea.getText());
				String title = getTitle();
				if (title.endsWith("*"))
					setTitle(title.substring(0, title.length() - 1));
			}
			catch (Client.BadPermissionsException x) {
				JOptionPane.showMessageDialog(null, x.getMessage());
			}
		}
	}

	private class ExitListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}
	}

	private class CopyListener implements ActionListener {

		private boolean cut;

		CopyListener(boolean cut) {
			this.cut = cut;
		}

		public void actionPerformed(ActionEvent e) {
			clipboard = textArea.getSelectedText();
			if (cut)
				textArea.replaceSelection("");

			// The next piece of code is adapted from the following SO page:
			// http://stackoverflow.com/questions/6710350/copying-text-to-the-clipboard-using-java
			Clipboard board = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection copyFoil = new StringSelection(KEYSTRING);
			board.setContents(copyFoil, null);
		}
	}

	private class PasteListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			Clipboard board = Toolkit.getDefaultToolkit().getSystemClipboard();
			String contents = "";
			try {
				contents = (String)(board.getContents(null).getTransferData(DataFlavor.stringFlavor));
			}
			catch (Exception x) {
				JOptionPane.showMessageDialog(null, "Error: Failed to display file.");
				setVisible(false);
			}

			String replacementText;
			if (contents != null) {
				if (contents.equals(KEYSTRING))
					replacementText = clipboard;
				else
					replacementText = contents;

				textArea.replaceSelection(replacementText);
			}
		}
	}

	private class FindListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
		}
	}

	private class ReplaceListener implements ActionListener {

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
}