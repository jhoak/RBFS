package client;

import java.util.LinkedList;

class RBFSFolder extends RBFSFile {

	private LinkedList<RBFSFile> childFiles;

	RBFSFolder(FileAttributes attributes, RBFSFolder parent, LinkedList<RBFSFile> childFiles) {
		super(attributes, parent);
		this.childFiles = (LinkedList<RBFSFile>)childFiles.clone();
	}

	LinkedList<RBFSFile> getChildFiles() {
		return (LinkedList<RBFSFile>)childFiles.clone();
	}

	void addParentLink() {
		FileAttributes newAtts = new FileAttributes("..", getAuthor(), getSize(), getDateMade(), getDateModded());
		childFiles.add(new RBFSFolder(
							newAtts,
							this,
							getParent().getChildFiles()
						)
					   );
	}

	@Override
	StorageType getStorageType() {
		return StorageType.FOLDER;
	}
}