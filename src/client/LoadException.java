package client;

import java.io.FileNotFoundException;

public class LoadException extends FileNotFoundException {
	public LoadException() {
		this(null);
	}

	public LoadException(String s) {
		super(s);
	}
}