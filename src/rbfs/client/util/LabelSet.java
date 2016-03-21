package rbfs.client.util;

import javax.swing.JLabel;

class LabelSet {

	private JLabel[] labels;

	LabelSet(JLabel... labels) {
		this.labels = labels;
	}

	void setText(String... strs) {
		if (strs.length != labels.length)
			throw new IllegalArgumentException("Error: unequal number of strings and labels");

		for (int i = 0; i < strs.length; i++)
			labels[i].setText(strs[i]);
	}
}
