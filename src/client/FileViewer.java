package client;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.function.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.event.*;

public class FileViewer extends JFrame {

	private static final String KEYSTRING = "Failed to paste text from RBFS file viewer!";
	private static final int WIDTH = 400,
							 HEIGHT = 300;

	private JTextArea textArea;
	private String clipboard;
	private boolean editable;
	private JPanel[] helperPanels;

	private enum Panels {
		FIND,
		FIND_REGEX,
		REPLACE,
		REPLACE_REGEX
	}

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

		helperPanels = new JPanel[4];
		helperPanels[Panels.FIND.ordinal()] = new FindPanel(false);
		helperPanels[Panels.FIND_REGEX.ordinal()] = new FindPanel(true);
		helperPanels[Panels.REPLACE.ordinal()] = new ReplacePanel(false);
		helperPanels[Panels.REPLACE_REGEX.ordinal()] = new ReplacePanel(true);

		for (int i = 0; i < helperPanels.length; i++)
			add(helperPanels[i], BorderLayout.SOUTH);
	}

	private JMenuBar makeMenuBar(FOConsumer saveFcn) {
		JMenu fileMenu = makeFileMenu(saveFcn);
		JMenu searchMenu = makeSearchMenu();

		JMenuBar menuBar = new JMenuBar();
		addComponents(menuBar, fileMenu);
		
		if (editable) {
			JMenu editMenu = makeEditMenu();
			addComponents(menuBar, editMenu);
		}
		addComponents(menuBar, searchMenu);
		return menuBar;
	}

	private JTextArea initializeTextArea(String fileContents) {
		JTextArea textArea = new JTextArea(fileContents);
		textArea.setLineWrap(true);
		textArea.setEditable(editable);
		textArea.getDocument().addDocumentListener(new TextChangedFrameListener());
		return textArea;
	}

	private JMenu makeFileMenu(FOConsumer saveFcn) {
		JMenu fileMenu = new JMenu("File");
		if (editable) {
			JMenuItem save = new JMenuItem("Save");
			addFunction(save, "ctrl S", new SaveListener(saveFcn));
			addComponents(fileMenu, save);
		}
		JMenuItem exit = new JMenuItem("Exit");
		addFunction(exit, "ctrl W", new ExitListener());
		addComponents(fileMenu, exit);
		return fileMenu;
	}

	private JMenu makeSearchMenu() {
		JMenu searchMenu = new JMenu("Search");
		JMenuItem find = new JMenuItem("Find"),
				  findRegex = new JMenuItem("Find Regular Expression");
		addFunction(find, "ctrl F", new FindListener(false));
		addFunction(findRegex, "ctrl shift F", new FindListener(true));
		addComponents(searchMenu, find, findRegex);

		if (editable) {
			JMenuItem replace = new JMenuItem("Replace"),
					  replaceRegex = new JMenuItem("Replace Regular Exp.");
			addFunction(replace, "ctrl H", new ReplaceListener(false));
			addFunction(replaceRegex, "ctrl shift H", new ReplaceListener(true)); 
			addComponents(searchMenu, replace, replaceRegex);
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

		addFunction(undo, "ctrl Z", new UndoListener());
		addFunction(redo, "ctrl Y", new RedoListener());
		addFunction(cut, "ctrl X", new CopyListener(true));
		addFunction(copy, "ctrl C", new CopyListener(false));
		addFunction(paste, "ctrl V", new PasteListener());

		addComponents(editMenu, undo, redo, cut, copy, paste);
		return editMenu;
	}

	private static void addComponents(Container container, Component... comps) {
		for (Component c : comps)
			container.add(c);
	}

	private static void addFunction(JMenuItem item, String hotkey, ActionListener listener) {
		item.setAccelerator(KeyStroke.getKeyStroke(hotkey));
		item.addActionListener(listener);
	}

	private void closeHelpers() {
		for (int i = 0; i < helperPanels.length; i++)
			helperPanels[i].setVisible(false);
	}

	private class FindListener implements ActionListener {

		private boolean useRegex;

		private FindListener(boolean useRegex) {
			this.useRegex = useRegex;
		}

		public void actionPerformed(ActionEvent e) {
			closeHelpers();

			if (useRegex)
				helperPanels[Panels.FIND_REGEX.ordinal()].setVisible(true);
			else
				helperPanels[Panels.FIND.ordinal()].setVisible(true);
		}
	}

	private class ReplaceListener implements ActionListener {

		private boolean useRegex;

		private ReplaceListener(boolean useRegex) {
			this.useRegex = useRegex;
		}

		public void actionPerformed(ActionEvent e) {
			closeHelpers();

			if (useRegex)
				helperPanels[Panels.REPLACE_REGEX.ordinal()].setVisible(true);
			else
				helperPanels[Panels.REPLACE.ordinal()].setVisible(true);
		}
	}

	private class FindPanel extends JPanel {

		private boolean useRegex;
		private JTextField findField;
		private JLabel countLabel;
		private StringFinder finder;
		private int numMatches, currentMatch;

		FindPanel(boolean useRegex) {
			this.useRegex = useRegex;

			String labelString = useRegex ? "Find Regular Expression:"
										  : "Find:";
			JLabel findLabel = new JLabel(labelString);
			findField = new JTextField(12);
			countLabel = new JLabel("0/0");
			JButton prev = new JButton("Previous"),
					next = new JButton("Next"),
					close = new JButton("Close");

			Runnable nextFcn = () -> next(),
					 prevFcn = () -> prev();
			next.addActionListener(new FindButtonListener(nextFcn));
			prev.addActionListener(new FindButtonListener(prevFcn));
			close.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					JPanel thisPanel = (JPanel)close.getParent();
					thisPanel.setVisible(false);
				}
			});

			Runnable updateTFMethod = () -> update(false);
			findField.getDocument().addDocumentListener(new TextChangedPanelListener(updateTFMethod));

			Runnable updateTAMethod = () -> update(true);
			textArea.getDocument().addDocumentListener(new TextChangedPanelListener(updateTAMethod));

			addComponents(this, findLabel, findField, countLabel, prev, next, close);
			setVisible(false);
		}

		private void update(boolean textAreaChanged) {
			if (findField.getText().equals("")) {
				countLabel.setText("0/0");
				numMatches = 0;
				currentMatch = 0;
				return;
			}

			finder = StringFinder.make(findField.getText(), textArea.getText(), useRegex);
			numMatches = finder.numMatches();
			if (numMatches == 0) {
				currentMatch = 0;
				countLabel.setText("0/0");
				textArea.setSelectionEnd(textArea.getSelectionStart());
			}
			else {
				currentMatch = 1;
				countLabel.setText("1/" + numMatches);
				int start = finder.start(),
					end = finder.end();
				textArea.select(start, end);
			}
		}

		private void next() {
			if (findField.getText().equals("") || numMatches == 0)
				return;

			finder.next();
			currentMatch = (currentMatch + 1) % numMatches;
		}

		private void prev() {
			if (findField.getText().equals("") || numMatches == 0)
				return;

			finder.previous();
			currentMatch = (currentMatch - 1) % numMatches;
		}

		private class FindButtonListener implements ActionListener {

			private Runnable action;

			FindButtonListener(Runnable action) {
				this.action = action;
			}

			public void actionPerformed(ActionEvent e) {
				action.run();

				countLabel.setText((currentMatch + 1) + "/" + numMatches);

				int start = finder.start(),
					end = finder.end();
				textArea.select(start, end);
			}
		}
	}

	private class ReplacePanel extends JPanel {
	}

	private class TextChangedFrameListener implements DocumentListener {
    
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

	private class TextChangedPanelListener implements DocumentListener {

		private Runnable updateMethod;

		private TextChangedPanelListener(Runnable updateMethod) {
			this.updateMethod = updateMethod;
		}

		public void insertUpdate(DocumentEvent e) {
        	updateMethod.run();
    	}
    	
    	public void removeUpdate(DocumentEvent e) {
    		updateMethod.run();
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

	private class UndoListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
		}
	}

	private class RedoListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
		}
	}
}