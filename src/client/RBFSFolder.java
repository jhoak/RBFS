package client;

import java.util.LinkedList;

class RBFSFolder extends RBFSFile {

	private LinkedList<RBFSFile> files;

	RBFSFolder(FileAttributes attrs) {
		super(attrs);
	}

	LinkedList<RBFSFile> getFiles() {
		return (LinkedList<RBFSFile>)files.clone();
	}

	void addFile(RBFSFile file) {
		files.add(file);
		file.setParent(this);
	}

	void addFiles(LinkedList<RBFSFile> files) {
		for (RBFSFile f : files)
			addFile(f);
	}

	@Override
	StorageType getStorageType() {
		return StorageType.FOLDER;
	}

	static RBFSFolder makeDirectoryTree(String input) {
		String[] tokens = input.split("\n");
		FileAttributes attrs = new FileAttributes("", "", "", "", "");
		RBFSFolder root = new RBFSFolder(attrs);
		root.addFiles(getFolderContents(tokens, 0, tokens.length - 1));
		return root;
	}

	static LinkedList<RBFSFile> getFolderContents(String[] tokens, int start, int end) {
		LinkedList<RBFSFile> contents = new LinkedList<>();
		int index = start;
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
				newFolder.addFiles(getFolderContents(tokens, index + 1, nextIndex - 1));
				contents.add(newFolder);
				index = nextIndex + 1;
			}
			else if (type.equals("FILE")) {
				RBFSFile file = new RBFSFile(attrs);
				contents.add(file);
			}
			else
				throw new IllegalArgumentException("ERROR: Bad input string.");
		}
		return contents;
	}
}