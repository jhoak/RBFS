package client;

class FileAttributes {

	private String name, author, size, dateMade, dateModded;

	FileAttributes(String name, String author, String size, String dateMade, String dateModded) {
		this.name = name;
		this.author = author;
		this.size = size;
		this.dateMade = dateMade;
		this.dateModded = dateModded;
	}

	String getName() {
		return name;
	}

	String getAuthor() {
		return author;
	}

	String getSize() {
		return size;
	}

	String getDateMade() {
		return dateMade;
	}

	String getDateModded() {
		return dateModded;
	}
}