package client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class FileViewer extends JFrame {

	static FileViewer make(String fileContents) {
		return new FileViewer(fileContents);
	}

	private FileViewer(String fileContents) {
	}
}