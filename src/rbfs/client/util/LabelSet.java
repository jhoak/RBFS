package rbfs.client.util;

import javax.swing.JLabel;

/**
A convenience class, LabelSet provides a way to store and edit lots of JLabels
at the same time. This is used in the FileMenu's GUI components to change the
file metadata labels in the lower left corner.

@author	James Hoak
*/

public class LabelSet {

	private JLabel[] labels;

	/**
	Constructs a new LabelSet comprising ONLY the given labels. The size of the
	set (and the labels themselves) are unchangeable except for their text.
	@param labels Zero or more JLabels to use in creating the set.
	*/
	public LabelSet(JLabel... labels) {
		this.labels = labels;
	}

	/**
	Sets the text of each label in the set; the i-th String argument is applied
	to the i-th JLabel in the set.
	@param strs The Strings to give to each JLabel in the set
	@throws IllegalArgumentException If the number of Strings and Labels differ
	*/
	public void setText(String... strs) {
		if (strs.length != labels.length)
			throw new IllegalArgumentException("Error: unequal number of strings and labels");

		for (int i = 0; i < strs.length; i++)
			labels[i].setText(strs[i]);
	}
}
