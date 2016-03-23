package rbfs.file;

/**
This class represents a type of virtual storage on  a computer -- a file or a
folder. It is used in the RBFSFile and RBFSFolder classes to distinguish the
different types for code that uses this package, as well as to help it produce
specific behaviors for each type.

@author	James Hoak
*/

public enum StorageType {
	FILE,
	FOLDER
}
