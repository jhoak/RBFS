package client;

import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import javax.swing.*;

class FileMenu extends JFrame {

	private LinkedList<String> roles;

	private FileMenu(LinkedList<String> roles) {
	}

	static FileMenu make(LinkedList<String> roles) {
		return new FileMenu(roles);
	}
}