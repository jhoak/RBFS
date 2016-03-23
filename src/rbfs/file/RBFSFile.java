package rbfs.file;

/**
This class resembles an openable file hosted by an RBFS server. Rather than
holding the file's actual contents, an instance of this class holds the
relevant file metadata (encapsulated by FileAttributes). Instances can be
queried to provide any of the FileAttributes it was built with, as well as
the parent folder if it exists.

@author	James Hoak
*/
public class RBFSFile {

	private FileAttributes attrs;			// File metadata
	private RBFSFolder parent;				// The folder containing this file

	/**
	Creates a new RBFSFile with the given metadata.
	@param attrs The file metadata to make the file with
	*/
	public RBFSFile(FileAttributes attrs) {
		this.attrs = attrs;
	}

	/**
	Gets the name of the file.
	@returns The name of the file.
	*/
	public String getName() {
		return attrs.getName();
	}

	/**
	Gets the name of the file's author.
	@returns The name of the file's author.
	*/
	public String getAuthor() {
		return attrs.getAuthor();
	}

	/**
	Gets the size of the file.
	@returns The size of the file.
	*/
	public String getSize() {
		return attrs.getSize();
	}

	/**
	Gets the date the file was created.
	@returns The date the file was created.
	*/
	public String getDateMade() {
		return attrs.getDateMade();
	}

	/**
	Gets the date the file was last modified.
	@returns The date the file was last modified.
	*/
	public String getDateModded() {
		return attrs.getDateModded();
	}

	/**
	Gets the parent folder of this file.
	@returns The parent folder of this file.
	*/
	public RBFSFolder getParent() {
		return parent;
	}

	/**
	Sets the parent of this file.
	@param parent The folder to contain this file
	*/
	public void setParent(RBFSFolder parent) {
		this.parent = parent;
	}

	/**
	Returns StorageType.FILE as this is a File, not a Folder.
	@return The enum value StorageType.FILE
	*/
	public StorageType getStorageType() {
		return StorageType.FILE;
	}
}
