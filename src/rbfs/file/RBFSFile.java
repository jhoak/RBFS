package rbfs.file;

class RBFSFile {

	private FileAttributes attrs;
	private RBFSFolder parent;

	RBFSFile(FileAttributes attrs) {
		this.attrs = attrs;
	}

	String getName() {
		return attrs.getName();
	}

	String getAuthor() {
		return attrs.getAuthor();
	}

	String getSize() {
		return attrs.getSize();
	}

	String getDateMade() {
		return attrs.getDateMade();
	}

	String getDateModded() {
		return attrs.getDateModded();
	}

	RBFSFolder getParent() {
		return parent;
	}

	void setParent(RBFSFolder parent) {
		this.parent = parent;
	}

	StorageType getStorageType() {
		return StorageType.FILE;
	}
}
