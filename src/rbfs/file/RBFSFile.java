package rbfs.file;

public class RBFSFile {

	private FileAttributes attrs;
	private RBFSFolder parent;

	public RBFSFile(FileAttributes attrs) {
		this.attrs = attrs;
	}

	public String getName() {
		return attrs.getName();
	}

	public String getAuthor() {
		return attrs.getAuthor();
	}

	public String getSize() {
		return attrs.getSize();
	}

	public String getDateMade() {
		return attrs.getDateMade();
	}

	public String getDateModded() {
		return attrs.getDateModded();
	}

	public RBFSFolder getParent() {
		return parent;
	}

	public void setParent(RBFSFolder parent) {
		this.parent = parent;
	}

	public StorageType getStorageType() {
		return StorageType.FILE;
	}
}
