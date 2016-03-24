package rbfs.file;

import java.util.LinkedList;

/**
This class resembles a special type of file which can hold other files/folders
inside (but cannot be opened in the file viewer on its own). Directory
structures made of RBFSFiles and Folders resemble realistic trees with each
folder having a built-in "subfolder" that moves the user up one directory when
opened.

@author	James Hoak
*/

public class RBFSFolder extends RBFSFile {

	private LinkedList<RBFSFile> files;		// The files this folder contains

	/**
	Creates a new Folder with the given metadata.
	@param attrs The file metadata to use for this folder.
	*/
	public RBFSFolder(FileAttributes attrs) {
		super(attrs);
	}

	/**
	Returns the list of Files this folder contains.
	@return The folder's files.
	*/
	public LinkedList<RBFSFile> getFiles() {
		return (LinkedList<RBFSFile>)files.clone();
	}

	/**
	Adds a file to this folder.
	@param file The file to add.
	*/
	public void addFile(RBFSFile file) {
		files.add(file);
		file.setParent(this);
	}

	/**
	Adds a list of files to this folder.
	@param files The files to add.
	*/
	public void addFiles(LinkedList<RBFSFile> files) {
		for (RBFSFile f : files)
			addFile(f);
	}

	/**
	Returns StorageType.FOLDER as this is a Folder, not an individual File.
	@return The enum value StorageType.FOLDER
	*/
	@Override
	public StorageType getStorageType() {
		return StorageType.FOLDER;
	}

	/**
	Constructs a "directory tree" based on the input and returns the root.

	The input String must be formatted as follows, with spaces replaced by \n
	(and with &lt; and &gt;):
		folder Folder1 Author1 64KB 12/1/15 1/1/16 &gt; file F1 Auth 4B 12/1/15 1/2/16 &lt;

	Files are declared one by one in the input String. Each declaration starts
	with the type (folder or file) followed by the file's metadata (name, author,
	size, date created, and date modified). If the type is a folder, this must
	be immediately followed by a &gt;, with zero or more subfiles/subfolders
	declared inside the folder. Then a &lt; character follows, and the folder is
	said to contain all of the files/folders declared within the brackets.

	The top-level declarations (i.e. ones that are NOT made within a folder)
	are used to construct the "root" folder, which is returned upon exiting
	this method.
	@param input The input string to be used in creating the directory.
	@return The root of the newly created virtual directory (not an actual
	directory on the local computer but rather representative of the one
	on the server).
	*/
	public static RBFSFolder makeDirectoryTree(String input) {
		String[] tokens = input.split("\n");
		FileAttributes attrs = new FileAttributes("", "", "", "", "");
		RBFSFolder root = new RBFSFolder(attrs);
		root.addFiles(getFolderContents(tokens, 0, tokens.length - 1));
		root.addParentLinksInTree();
		return root;
	}

	/**
	Using a set of tokens and a range to use (inclusive for the start and end
	points), constructs the contents of a folder with files in the given range
	and returns the contents as a list of RBFSFiles.
	@param tokens The tokens of the input string.
	@param start The start of the range of tokens to use
	@param end The end of the range to use
	@return The list of files and folders defined in the range of tokens.
	@throws IllegalArgumentException If the input string is invalid.
	*/
	private static LinkedList<RBFSFile> getFolderContents(String[] tokens, int start, int end) {
		LinkedList<RBFSFile> contents = new LinkedList<>();
		int index = start;
		// Read declarations until we leave the given range
		while (index <= end) {
			String type = tokens[index],
				   name = tokens[index + 1],
				   auth = tokens[index + 2],
				   size = tokens[index + 3],
				   dateMade = tokens[index + 4],
				   dateModded = tokens[index + 5];
			FileAttributes attrs = new FileAttributes(name, auth, size, dateMade, dateModded);
			index += 6;

			if (type.equals("FOLDER")) {
				/* Parse the rest of the tokens (up until a '<') as the
				contents of a new folder, then make the folder using a
				recursive process and add it to the contents list.   */
				int nextIndex = index;
				RBFSFolder newFolder = new RBFSFolder(attrs);
				int bracketCount = 0;
				while (!tokens[nextIndex].equals("<") || bracketCount != 1) {
					if (tokens[nextIndex].equals("<"))
						bracketCount--;
					else if (tokens[nextIndex].equals(">"))
						bracketCount++;
					nextIndex++;
				}
				// Get the folder contents in the range we found and add them
				// to a new folder
				newFolder.addFiles(getFolderContents(tokens, index + 1, nextIndex - 1));
				contents.add(newFolder);
				index = nextIndex + 1;
			}
			// If we just have a file, then just make the metadata and add the
			// file to the list of contents
			else if (type.equals("FILE")) {
				RBFSFile file = new RBFSFile(attrs);
				contents.add(file);
			}
			else
				throw new IllegalArgumentException("ERROR: Bad input string.");
		}
		return contents;
	}

	/**
	Add subdirectories that link up one directory (so the user can actually
	go back a folder if they go too deep.
	*/
	private void addParentLinksInTree() {
		for (RBFSFile f : files) {
			if (f.getStorageType() == StorageType.FOLDER) {
				RBFSFolder folder = (RBFSFolder)f;

				// Recursively add links for each subfolder
				folder.addParentLinksInTree();
				folder.addParentLink();
			}
		}
	}

	/**
	Adds a subfolder that goes up one directory.
	*/
	private void addParentLink() {
		RBFSFolder parent = getParent();
		String name = parent.getName();

		// First, make the name of the subdirectory obviously a link up the
		// directory (i.e., ".. (parentFolder)").
		String newName;
		if (name.startsWith(".. (") && name.endsWith(")"))
			newName = name;
		else
			newName = ".. (" + name + ")";

		// Make the relevant metadata
		String auth = parent.getAuthor(),
			   size = parent.getSize(),
			   dateMade = parent.getDateMade(),
			   dateModded = parent.getDateModded();
		FileAttributes newAttrs = new FileAttributes(newName, auth, size, dateMade, dateModded);

		// Make the folder with our new metadata, add files to it and add that
		// file to this folder
		RBFSFolder parentLink = new RBFSFolder(newAttrs);
		parentLink.addFiles(parent.getFiles());
		addFile(parentLink);
	}
}
