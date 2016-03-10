package client;

class RBFSFile {

	private FileAttributes attributes;
	private RBFSFolder parent;

	RBFSFile(FileAttributes attributes, RBFSFolder parent) {
		this.attributes = attributes;
		this.parent = parent;
	}

	String getName() {
		return attributes.getName();
	}

	String getAuthor() {
		return attributes.getAuthor();
	}

	String getSize() {
		return attributes.getSize();
	}

	String getDateMade() {
		return attributes.getDateMade();
	}

	String getDateModded() {
		return attributes.getDateModded();
	}

	RBFSFolder getParent() {
		return parent;
	}

	StorageType getStorageType() {
		return StorageType.FILE;
	}
}