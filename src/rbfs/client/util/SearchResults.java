package rbfs.client.util;

import java.util.LinkedList;

public class SearchResults {

	private IntPair[] matches;
	private int matchIndex;

	public SearchResults(LinkedList<IntPair> matchList) {
		matches = new IntPair[matchList.size()];
		int i = 0;
		for (IntPair pair : matchList) {
			matches[i] = pair;
			i++;
		}

		matchIndex = 0;
	}

	public int numMatches() {
		return matches.length;
	}

	public IntPair[] getMatches() {
		IntPair[] matchesNew = new IntPair[matches.length];
		for (int i = 0; i < matches.length; i++)
			matchesNew[i] = matches[i];
		return matchesNew;
	}

	public IntPair currentMatch() {
		return matches[matchIndex];
	}

	public IntPair nextMatch() {
		if (matches.length != 0)
			matchIndex = (matchIndex + 1) % matches.length;
		return currentMatch();
	}

	public IntPair previousMatch() {
		if (matches.length != 0)
			matchIndex = (matchIndex - 1) % matches.length;
		return currentMatch();
	}
}
