package client;

import java.util.LinkedList;

class SearchResults {

	private IntPair[] matches;
	private int matchIndex;

	SearchResults(LinkedList<IntPair> matchList) {
		matches = new IntPair[matchList.size()];
		int i = 0;
		for (IntPair pair : matchList) {
			matches[i] = pair;
			i++;
		}

		matchIndex = 0;
	}

	int numMatches() {
		return matches.length;
	}

	IntPair[] getMatches() {
		IntPair[] matchesNew = new IntPair[matches.length];
		for (int i = 0; i < matches.length; i++)
			matchesNew[i] = matches[i];
		return matchesNew;
	}

	IntPair currentMatch() {
		return matches[matchIndex];
	}

	IntPair nextMatch() {
		if (matches.length != 0)
			matchIndex = (matchIndex + 1) % matches.length;
		return currentMatch();
	}

	IntPair previousMatch() {
		if (matches.length != 0)
			matchIndex = (matchIndex - 1) % matches.length;
		return currentMatch();
	}
}
