package client;

import java.util.regex.*;

class StringFinder {

	private IntPair[] matches;
	private int matchIndex;

	static StringFinder make(String toFind, String toSearch, boolean regex) {
		return new StringFinder(toFind, toSearch, regex);
	}

	private StringFinder(String toFind, String toSearch, boolean regex) {
		LinkedList<IntPair> matchList = new LinkedList<>();

		if (regex) {
			Matcher m = Pattern.compile(toFind).matcher(toSearch);
			while (!m.hitEnd()) {
				if (m.find()) {
					int start = m.start(),
						end = m.end();
					matchList.add(new IntPair(start, end));
				}
			}
		}
		else {
			int searchStartIndex = 0;
			int nextMatchStartIndex = toSearch.indexOf(toFind, 0);
			while (nextMatchStartIndex != -1) {
				int start = nextMatchStartIndex,
					end = start + toFind.length();
				matchList.add(new IntPair(start, end));

				searchStartIndex = end + 1;
				nextMatchStartIndex = toSearch.indexOf(toFind, searchStartIndex);
			}
		}

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

	int start() {
		if (matches.length != 0)
			return matches[matchIndex].getFirst();
		else
			return -1;
	}

	int end() {
		if (matches.length != 0)
			return matches[matchIndex].getLast();
		else
			return -1;
	}

	void next() {
		if (matches.length != 0)
			matchIndex = (matchIndex + 1) % matches.length;
	}

	void previous() {
		if (matches.length != 0)
			matchIndex = (matchIndex - 1) % matches.length;
	}

	private static class IntPair {
		
		private int first, second;

		IntPair(int first, int second) {
			this.first = first;
			this.second = second;
		}

		int getFirst() {
			return first;
		}

		int getLast() {
			return last;
		}
	}
}