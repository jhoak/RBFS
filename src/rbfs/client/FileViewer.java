package rbfs.client;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;
import rbfs.client.fcn.*;
import rbfs.client.util.*;

/**
This window allows users to read or write a selected file, given that they have
the appropriate permissions to open it in the first place. If writing is
enabled, the user may edit the file currently being read, using the viewer as a
lightweight text editor.

@author	James Hoak
*/

public class FileViewer extends JFrame {

	// The following String is put on the user's clipboard when they try to
	// copy text from a RBFS file and paste it elsewhere:
	private static final StringSelection KEYSTRING = new StringSelection(
		"Failed to paste text from RBFS file viewer!"
	);
	private static final int WIDTH = 400,	// window width
							 HEIGHT = 300;	// and height

	// Window components and some other required fields
	private JTextArea textArea;
	private JPanel[] helperPanels;
	private UndoManager undoMgr;
	private String savedText;
	private boolean editable;

	// Represents the finder panels used in this application for searching and
	// replacing text. These are used in an array of panels to set then visible
	// and invisible.
	private enum Panels {
		FIND,
		FIND_REGEX,
		REPLACE,
		REPLACE_REGEX
	}

	/**
	Factory method to initialize a new window for viewing a file.
	@param editable Whether or not the user may overwrite the file's contents.
	@param fileContents The file's contents, as a String
	@param saveFcn The function used to save data back to the server
	@return A new FileViewer window.
	*/
	static FileViewer make(boolean editable, String fileContents, FOConsumer saveFcn) {
		return new FileViewer(editable, fileContents, saveFcn);
	}

	/**
	Creates a new window for viewing a file. If the user may not edit the file,
	the window will simply list the files contents in an uneditable text area
	with some extra non-editing functionality added (i.e. finding). Otherwise,
	if the file is editable, the window acts as a lightweight text editor,
	supporting more complex functions to edit the file and save the contents
	back to the server.
	@param editable Whether or not the user may overwrite the file's contents.
	@param fileContents The file's contents, as a String
	@param saveFcn The function used to save data back to the server
	*/
	private FileViewer(boolean editable, String fileContents, FOConsumer saveFcn) {
		// Important fields to set
		this.editable = editable;
		undoMgr = new UndoManager();

		// Window attributes
		setTitle("File Viewer");
		setSize(WIDTH, HEIGHT);
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int)(screenDim.getWidth() / 4.0), (int)(screenDim.getHeight() / 4.0));
		setDefaultCloseOperation(HIDE_ON_CLOSE);

		// GUI Setup
		textArea = initializeTextArea(fileContents);
		JScrollPane scrollPane = new JScrollPane(textArea);
		add(scrollPane, BorderLayout.CENTER);

		JMenuBar menuBar = makeMenuBar(saveFcn);
		add(menuBar, BorderLayout.NORTH);

		// Find and replace panels, which are hidden initially
		helperPanels = new JPanel[4];
		helperPanels[Panels.FIND.ordinal()] = new FindPanel(false);
		helperPanels[Panels.FIND_REGEX.ordinal()] = new FindPanel(true);
		helperPanels[Panels.REPLACE.ordinal()] = new ReplacePanel(false);
		helperPanels[Panels.REPLACE_REGEX.ordinal()] = new ReplacePanel(true);

		for (int i = 0; i < helperPanels.length; i++)
			add(helperPanels[i], BorderLayout.SOUTH);
	}

	/**
	Creates the menu bar at the top of the window.
	@param saveFcn The function to use when saving the file back to the server
	@return The new menu bar to use
	*/
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

	/**
	Creates the text area used to view the file.
	@param fileContents The text of the given file
	@return A text area that displays the file's contents
	*/
	private JTextArea initializeTextArea(String fileContents) {
		JTextArea textArea = new JTextArea(fileContents);
		textArea.setLineWrap(true);
		textArea.setEditable(editable);

		Document document = textArea.getDocument();
		document.addDocumentListener(new TextChangedFrameListener());
		document.addUndoableEditListener(new EditListener());
		return textArea;
	}

	/**
	Creates the "File" tab of the menu bar, containing functions for saving and
	quitting.
	@param saveFcn The function to use to save the file to the server
	@return The file tab to be put in the menu bar.
	*/
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

	/**
	Makes the menu used to search throughout the document for text,
	whether it is a regular expression or just a literal.
	@return The search tab to be added to the menu bar
	*/
	private JMenu makeSearchMenu() {
		JMenu searchMenu = new JMenu("Search");
		JMenuItem find = new JMenuItem("Find"),
				  findRegex = new JMenuItem("Find Regular Expression");
		addFunction(find, "ctrl F", new FindListener(false));
		addFunction(findRegex, "ctrl shift F", new FindListener(true));
		addComponents(searchMenu, find, findRegex);

		// Only add the replace function if this document is editable
		if (editable) {
			JMenuItem replace = new JMenuItem("Replace"),
					  replaceRegex = new JMenuItem("Replace Regular Exp.");
			addFunction(replace, "ctrl H", new ReplaceListener(false));
			addFunction(replaceRegex, "ctrl shift H", new ReplaceListener(true)); 
			addComponents(searchMenu, replace, replaceRegex);
		}
		return searchMenu;
	}

	/**
	Make the menu tab used to provide different functions for editing the file.
	@return The edit menu tab to be added to the menu bar
	*/
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

	/**
	Given a container and a set of components, add all components to the container.
	@param container The container to put stuff in
	@param comps The components to add
	*/
	private static void addComponents(Container container, Component... comps) {
		for (Component c : comps)
			container.add(c);
	}

	/**
	Given a JMenuItem, hotkey and listener, adds the hotkey and listener
	to the menu item.
	@param item The item to add stuff to
	@param hotkey The hotkey to add
	@param listener The action listener to associate with this item
	*/
	private static void addFunction(JMenuItem item, String hotkey, ActionListener listener) {
		item.setAccelerator(KeyStroke.getKeyStroke(hotkey));
		item.addActionListener(listener);
	}

	/**
	Closes all open find and replace panels.
	*/
	private void closeHelpers() {
		for (int i = 0; i < helperPanels.length; i++)
			helperPanels[i].setVisible(false);
	}

	/**
	This listener opens a finder menu when fired. Depending on the flag of this
	listener, will open a finder for plaintext or regular expressions.
	*/
	private class FindListener implements ActionListener {

		private boolean useRegex;

		/**
		Constructs a new listener that may or may not use regular expressions
		in its search.
		@param useRegex Whether or not to search for the text as a regular
		expression.
		*/
		private FindListener(boolean useRegex) {
			this.useRegex = useRegex;
		}

		/**
		When fired, set the appropriate panel visible and all others
		invisible.
		@param e The event that fired this listener
		*/
		public void actionPerformed(ActionEvent e) {
			closeHelpers();

			if (useRegex)
				helperPanels[Panels.FIND_REGEX.ordinal()].setVisible(true);
			else
				helperPanels[Panels.FIND.ordinal()].setVisible(true);
		}
	}

	/**
	This listener opens replacer panels. Like the finder panels, may or may not
	use regular expressions.
	*/
	private class ReplaceListener implements ActionListener {

		private boolean useRegex;

		/**
		Creates a new listener which may or may not use regexes according to the
		argument.
		@param useRegex Whether or not to search using regular expressions
		*/
		private ReplaceListener(boolean useRegex) {
			this.useRegex = useRegex;
		}

		/**
		When fired, opens the relevant replacer panel
		@param e The event that fired this listener
		*/
		public void actionPerformed(ActionEvent e) {
			closeHelpers();

			if (useRegex)
				helperPanels[Panels.REPLACE_REGEX.ordinal()].setVisible(true);
			else
				helperPanels[Panels.REPLACE.ordinal()].setVisible(true);
		}
	}

	/**
	A panel that allows the user to search for text in the document.
	*/
	private class FindPanel extends JPanel {

		// Window components and other necessary fields
		private JTextField findField;
		private JLabel countLabel;
		private boolean useRegex;
		private IntPair[] matches;
		private int selectedMatchIndex;

		/**
		Creates a new panel that can search for either regular expressions or
		plaintext, depending on the flag argument.
		@param useRegex Whether or not to search using regular expressions
		*/
		FindPanel(boolean useRegex) {
			this.useRegex = useRegex;
			matches = new IntPair[0];
			selectedMatchIndex = 0;

			// GUI Components
			String labelString = useRegex ? "Find Regular Expression:"
										  : "Find:";
			JLabel findLabel = new JLabel(labelString);
			findField = new JTextField(12);
			countLabel = new JLabel("0/0");
			JButton prev = new JButton("Previous"),
					next = new JButton("Next"),
					close = new JButton("Close");

			// Add functionality (ha) to components
			Runnable nextFcn = () -> next(),
					 prevFcn = () -> prev();
			next.addActionListener(new FindButtonListener(nextFcn));
			prev.addActionListener(new FindButtonListener(prevFcn));

			// Need to close the panel when the close button is clicked
			close.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					JPanel thisPanel = (JPanel)close.getParent();
					thisPanel.setVisible(false);
				}
			});

			// Provide methods to update the panel when there are changes in either
			// the document or the text to search for
			Runnable updateTFMethod = () -> update(false);
			findField.getDocument().addDocumentListener(new TextChangedPanelListener(updateTFMethod));

			Runnable updateTAMethod = () -> update(true);
			textArea.getDocument().addDocumentListener(new TextChangedPanelListener(updateTAMethod));

			addComponents(this, findLabel, findField, countLabel, prev, next, close);
			setVisible(false);
		}

		/**
		Return the matches found using this panel
		@return The set of matches that the search yielded
		*/
		IntPair[] getMatches() {
			return matches;
		}

		/**
		Return the match that is currently highlighted in the document
		@return An IntPair that describes the start and end of the match
		*/
		IntPair getSelectedMatch() {
			if (matches.length != 0)
				return matches[selectedMatchIndex];
			else
				return null;
		}

		/**
		Advances the match index (if there are actually matches to examine).
		*/
		private void next() {
			if (findField.getText().equals("") || matches.length == 0)
				return;

			selectedMatchIndex = (selectedMatchIndex + 1) % matches.length;
		}

		/**
		Moves the match index backward (if there are matches to examine).
		*/
		private void prev() {
			if (findField.getText().equals("") || matches.length == 0)
				return;

			selectedMatchIndex = (selectedMatchIndex - 1) % matches.length;
			if (selectedMatchIndex < 0)
				selectedMatchIndex += matches.length;
		}

		/**
		Update the matches we found when the document or the text to find changes.
		@param textAreaChanged Whether or not the file's contents changed. (If
		false, the text to search for has changed.)
		*/
		private void update(boolean textAreaChanged) {
			if (findField.getText().equals("")) {
				// If there's nothing to find, there are no matches.
				countLabel.setText("0/0");
				matches = new IntPair[0];
				selectedMatchIndex = 0;
				textArea.setSelectionEnd(textArea.getSelectionStart());
				return;
			}

			// Now that we have stuff to search for, perform the search and get
			// the results.
			SearchResults results = StringFinder.search(findField.getText(), textArea.getText(), useRegex);
			matches = results.getMatches();

			// If there are no matches, do as above
			if (matches.length == 0) {
				selectedMatchIndex = 0;
				countLabel.setText("0/0");
				textArea.setSelectionEnd(textArea.getSelectionStart());
			}
			// Otherwise, set our index to the first match and select it
			else {
				selectedMatchIndex = 0;
				countLabel.setText("1/" + matches.length);
				int start = matches[0].getFirst(),
					end = matches[0].getSecond();
				textArea.select(start, end);
			}
		}

		/**
		Listens for clicks of the "Find" buttons in the panel.
		*/
		private class FindButtonListener implements ActionListener {

			private Runnable action;

			/**
			Creates a new listener with the given action to perform on click.
			@param axn The action to perform when a find button is clicked.
			*/
			private FindButtonListener(Runnable axn) {
				action = axn;
			}

			/**
			Runs the action and changes the label and current match as appropriate.
			*/
			public void actionPerformed(ActionEvent e) {
				action.run();

				countLabel.setText((selectedMatchIndex + 1) + "/" + matches.length);

				IntPair match = matches[selectedMatchIndex];
				int start = match.getFirst(),
					end = match.getSecond();
				textArea.select(start, end);
			}
		}
	}

	/**
	A panel that allows the user to both find AND replace text.
	*/
	private class ReplacePanel extends JPanel {

		private FindPanel findPanel;		// the finder subpanel used for this panel
		private JTextField replaceField;	// the field containing the replacement text

		/**
		Creates a new panel and enables/disables searching with regular expressions.
		@param useRegex Whether or not to search with regular expressions
		*/
		ReplacePanel(boolean useRegex) {
			setLayout(new GridLayout(2,1));
			findPanel = new FindPanel(useRegex);
			JPanel lowerPanel = buildLowerPanel();
			addComponents(this, findPanel, lowerPanel);
			setVisible(false);
		}

		/**
		Builds the subpanel that actually performs the replacement operations.
		@return A panel that holds the replacement text and buttons to replace matches with.
		*/
		private JPanel buildLowerPanel() {
			// Create the leftmost label for the panel
			JLabel repLabel = new JLabel("Replace with:");
			replaceField = new JTextField(12);

			// Create the replace buttons + give them functionality
			JButton replace = new JButton("Replace"),
					replaceAll = new JButton("Replace All");

			replace.addActionListener(new ReplaceListener());
			replaceAll.addActionListener(new ReplaceAllListener());

			// Build the button subpanel and the whole lower panel now
			JPanel buttonPanel = new JPanel(new GroupLayout(this));
			addComponents(buttonPanel, replace, replaceAll);

			JPanel lowerPanel = new JPanel();
			addComponents(lowerPanel, repLabel, replaceField, buttonPanel);
			return lowerPanel;
		}

		/**
		Listens for button clicks on the "Replace" button.
		*/
		private class ReplaceListener implements ActionListener {
			
			/**
			Performs a single replacement operation when clicked.
			@param e The click event that fired this listener
			*/
			public void actionPerformed(ActionEvent e) {
				IntPair match = findPanel.getSelectedMatch();
				if (match != null) {
					textArea.select(match.getFirst(), match.getSecond());
					textArea.replaceSelection(replaceField.getText());
				}
			}
		}

		/**
		Listens for clicks on the "Replace All" button.
		*/
		private class ReplaceAllListener implements ActionListener {
			
			/**
			Performs one replace operation for each match found in the document.
			@param e The click event that fired this listener
			*/
			public void actionPerformed(ActionEvent e) {
				IntPair[] matches = findPanel.getMatches();
				if (matches.length != 0) {
					String replacementText = replaceField.getText();
					for (int i = matches.length - 1; i >= 0; i--) {
						IntPair match = matches[i];
						textArea.select(match.getFirst(), match.getSecond());
						textArea.replaceSelection(replacementText);
					}
				}
			}
		}
	}

	/**
	Listens for changes in the text area's contents (the file).
	*/
	private class TextChangedFrameListener implements DocumentListener {
    
    	/**
    	On insert, just do the same thing as removal (see below).
    	@param e The change that fired this listener
    	*/
    	public void insertUpdate(DocumentEvent e) {
        	removeUpdate(e);
    	}
    	
    	/**
    	When the document changes after a save, change the title to let the user
    	know it has not been saved (yet).
    	@param e The change that fired this listener
    	*/
    	public void removeUpdate(DocumentEvent e) {
    		setTitle(getTitle() + "*");
    	}
    	
    	/**
    	Does nothing. This method is never called by the TextArea's listener, as
    	the TextArea only shows plaintext without formatting.
    	@param e The change that fired this listener
    	*/
    	public void changedUpdate(DocumentEvent e) {}
	}

	/**
	Listens for changes in the finder panels' text fields (the string to search for).
	*/
	private class TextChangedPanelListener implements DocumentListener {

		private Runnable updateMethod;

		/**
		Creates a new listener given a method that runs an update.
		@param updateMethod A method this listener runs when performing an update.
    	*/
    	private TextChangedPanelListener(Runnable updateMethod) {
			this.updateMethod = updateMethod;
		}

		/**
		Same as removeUpdate(). Simply runs the update method.
    	@param e The change that fired this listener
    	*/
    	public void insertUpdate(DocumentEvent e) {
        	updateMethod.run();
    	}
    	
    	/**
		Same as insertUpdate(). Simply runs the update method.
    	@param e The change that fired this listener
    	*/
    	public void removeUpdate(DocumentEvent e) {
    		updateMethod.run();
    	}
    	
    	/**
    	Does nothing. This method is never called by the TextField's listener, as
    	the TextField only shows plaintext without formatting.
    	@param e The change that fired this listener
    	*/
    	public void changedUpdate(DocumentEvent e) {}
	}

	/**
	A listener that handles saving the file when the user chooses to do so.
	*/
	private class SaveListener implements ActionListener {

		private FOConsumer saveFcn;

		/**
		Creates a new listener using the given method to save data.
		@param saveFcn The method to use to save the file's data
		*/
		SaveListener(FOConsumer saveFcn) {
			this.saveFcn = saveFcn;
		}

		/**
		Tries to save changes and change the title if the save worked.
		@param e The event that caused this listener to fire
		*/
		public void actionPerformed(ActionEvent e) {
			try {
				saveFcn.accept(textArea.getText());

				// The following only runs if the accept() method worked without
				// throwing an exception.
				String title = getTitle();
				if (title.endsWith("*"))
					setTitle(title.substring(0, title.length() - 1));
			}
			catch (BadPermissionsException x) {
				// Show an error message with the given exception message
				JOptionPane.showMessageDialog(null, x.getMessage());
			}
		}
	}

	/**
	Listens for the user to close the window.
	*/
	private class ExitListener implements ActionListener {

		/**
		On close, just set the window invisible. 
		@param e The event that fired this listener
		*/
		public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}
	}

	/**
	Listens for copy commands from the user.

	Copying in the FileViewer copies the selected text to an internal clipboard
	and saves a different set of text to the user's clipboard. (This is done to
	prevent users from simulating the effect of opening multiple files that would
	normally be inaccessible at the same time due to role conflicts.)
	*/
	private class CopyListener implements ActionListener {

		private boolean cut;

		/**
		Creates a new listener that, depending on the flag, may or may not cut
		selected text on command.
		@param cut Whether or not to delete text on copy.
		*/
		CopyListener(boolean cut) {
			this.cut = cut;
		}

		/**
		Saves the selected text to the internal clipboard as well as some fake
		text to the user's normal clipboard. If cut is true, then the selected
		text is also deleted.
		*/
		public void actionPerformed(ActionEvent e) {
			savedText = textArea.getSelectedText();
			if (cut)
				textArea.replaceSelection("");

			Clipboard board = Toolkit.getDefaultToolkit().getSystemClipboard();
			board.setContents(KEYSTRING, null);
		}
	}

	/**
	Listens for paste commands from the user.

	The program must decide whether to paste from the viewer's internal clipboard
	or the user's clipboard. If the user's text equals the keystring defined at
	the start of the class (see KEYSTRING), then the internal clipboard is used.
	Otherwise, the user's clipboard is used.
	*/
	private class PasteListener implements ActionListener {

		/**
		Pastes text into the document.
		@param e The event that fired this listener
		*/
		public void actionPerformed(ActionEvent e) {
			
			// Check the system clipboard first. If that fails, have to close.
			Clipboard board = Toolkit.getDefaultToolkit().getSystemClipboard();
			String contents = "";
			try {
				contents = (String)(board.getContents(null).getTransferData(DataFlavor.stringFlavor));
			}
			catch (Exception x) {
				JOptionPane.showMessageDialog(null, "Error: Failed to display file.");
				setVisible(false);
				return;
			}

			// Now decide which clipboard to use.
			if (contents != null) {
				String replacementText;
				if (contents.equals(KEYSTRING))
					replacementText = savedText;
				else
					replacementText = contents;

				textArea.replaceSelection(replacementText);
			}
		}
	}

	/**
	Listens for undo commands from the user.
	*/
	private class UndoListener implements ActionListener {

		/**
		When fired, this undoes the last undoable action registered to the
		undoManager. If we can't undo it, just do nothing.
		@param e The event that fired this listener
		*/
		public void actionPerformed(ActionEvent e) {
			try {
				undoMgr.undo();
			}
			catch (CannotUndoException x) {
				// Do nothing. We're good here!
			}
		}
	}

	/**
	Listens for redo commands from the user.
	*/
	private class RedoListener implements ActionListener {

		/**
		When fired, this redoes the last redoable action registered to the
		undoManager. If we can't redo it, just do nothing.
		@param e The event that fired this listener
		*/
		public void actionPerformed(ActionEvent e) {
			try {
				undoMgr.redo();
			}
			catch (CannotRedoException x) {
				// Do nothing. We're good here!
			}
		}
	}

	/**
	Listens for user (or internal) edits to the document that may be undone.
	*/
	private class EditListener implements UndoableEditListener {
		
		/**
		When an undoable edit occurs (i.e. deleting text), this registers the
		event with the undo manager so that it might be undone at the user's
		request.
		@param e The event that fired this listener
		*/
		public void undoableEditHappened(UndoableEditEvent e) {
			undoMgr.addEdit(e.getEdit());
        }
	}
}
