package rbfs.client.util;

import javax.swing.JLabel;

public class LabelSet {

	private JLabel[] labels;

	public LabelSet(JLabel... labels) {
		this.labels = labels;
	}

	public void setText(String... strs) {
		if (strs.length != labels.length)
			throw new IllegalArgumentException("Error: unequal number of strings and labels");

		for (int i = 0; i < strs.length; i++)
			labels[i].setText(strs[i]);
	}
}
