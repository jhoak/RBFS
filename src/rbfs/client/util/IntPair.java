package rbfs.client.util;

/**
As simple as the name implies, instances of this class are simply pairs of
ints. They are necessarily immutable, but creation of new pairs is trivial.

@author	James Hoak
*/

public class IntPair {

	private int first, second;

	/**
	Constructs a new pair of ints, given a pair of ints. HA.
	@param first The first int of the pair.
	@param second The second int of the pair.
	*/
	public IntPair(int first, int second) {
		this.first = first;
		this.second = second;
	}

	/**
	Gets the first int.
	@return Returns the first int.
	*/
	public int getFirst() {
		return first;
	}

	/**
	Gets the second int.
	@return Returns the second int.
	*/
	public int getSecond() {
		return second;
	}
}
