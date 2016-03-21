package rbfs.file;

public class FileAttributes {

	private String name, author, size, dateMade, dateModded;

	public FileAttributes(String name, String author, String size, String dateMade, String dateModded) {
		this.name = name;
		this.author = author;
		this.size = size;
		this.dateMade = dateMade;
		this.dateModded = dateModded;
	}

	public String getName() {
		return name;
	}

	public String getAuthor() {
		return author;
	}

	public String getSize() {
		return size;
	}

	public String getDateMade() {
		return dateMade;
	}

	public String getDateModded() {
		return dateModded;
	}
}
