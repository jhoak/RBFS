package rbfs.client.util;

import java.util.LinkedList;

/**
An instance of this class describes the results obtained via a search of one
string for some other substring (regex. or not). Users of this class may simply
get the array of matches and look through it themselves, or use the class's
methods to sift through the results instead of transferring the data.

@author	James Hoak
*/

public class SearchResults {

	private IntPair[] matches;	// the list of matches found (circular array)
	private int matchIndex;		// index of the match we're currently examining

	/**
	Create a new set of results based on the list of matches.
	@param matchList The list of matches to use
	*/
	public SearchResults(LinkedList<IntPair> matchList) {
		matches = new IntPair[matchList.size()];
		int i = 0;
		for (IntPair pair : matchList) {
			matches[i] = pair;
			i++;
		}

		matchIndex = 0;
	}

	/**
	Return the number of matches found.
	@return The number of matches found
	*/
	public int numMatches() {
		return matches.length;
	}

	/**
	Return all of the matches found.
	@return An array containing all the matches that were found
	*/
	public IntPair[] getMatches() {
		IntPair[] matchesNew = new IntPair[matches.length];
		for (int i = 0; i < matches.length; i++)
			matchesNew[i] = matches[i];
		return matchesNew;
	}

	/** 
	Return the match we're currently looking at.
	@return The match we're currently looking at (ha).
	*/
	public IntPair currentMatch() {
		return matches[matchIndex];
	}

	/**
	Gets the match after the current match and increments the match index.
	@return The match that is after the current match
	*/
	public IntPair nextMatch() {
		if (matches.length != 0)
			matchIndex = (matchIndex + 1) % matches.length;
		return currentMatch();
	}

	/**
	Gets the match preceding the current match and decrements the match index.
	@return The match that comes before the current match
	*/
	public IntPair previousMatch() {
		if (matches.length != 0)
			matchIndex = (matchIndex - 1) % matches.length;
		return currentMatch();
	}
}
