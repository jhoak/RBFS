package client;

import java.util.LinkedList;
import java.util.regex.*;

class StringFinder {

	private StringFinder() {}

	static SearchResults search(String toFind, String toSearch, boolean regex) {
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
		return new SearchResults(matchList);
	}
}