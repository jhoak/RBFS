package rbfs.file;

/**
This class holds file metadata used to describe both RBFSFiles and Folders:
the file's name, author, size, date of creation, and the date of the most
recent modification. In practice, these data are provided by the server (via
a substring of one long string) and it is the client's data to interpret them
and construct new instances of the FileAttributes class.

@author	James Hoak
*/

public class FileAttributes {

	private String name, author, size, dateMade, dateModded;

	/**
	Returns a new set of attributes with the given metadata as parameters.
	@param name The name of the file
	@param author The author of the file
	@param size The size of the file. Must include units of storage.
	@param dateMade The date the file was created
	@param dateModded The date the file was last modified
	*/
	public FileAttributes(String name, String author, String size, String dateMade, String dateModded) {
		this.name = name;
		this.author = author;
		this.size = size;
		this.dateMade = dateMade;
		this.dateModded = dateModded;
	}

	/**
	Gets the name of the file.
	@returns The name of the file.
	*/
	public String getName() {
		return name;
	}

	/**
	Gets the name of the file's author.
	@returns The name of the file's author.
	*/
	public String getAuthor() {
		return author;
	}

	/**
	Gets the size of the file.
	@returns The size of the file.
	*/
	public String getSize() {
		return size;
	}

	/**
	Gets the date the file was created.
	@returns The date the file was created.
	*/
	public String getDateMade() {
		return dateMade;
	}

	/**
	Gets the date the file was last modified.
	@returns The date the file was last modified.
	*/
	public String getDateModded() {
		return dateModded;
	}
}
