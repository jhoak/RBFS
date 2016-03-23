package rbfs.client.util;

import java.util.LinkedList;
import java.util.regex.*;

/**
Given two strings -- a String to look for, and a String to search -- as well as
whether or not to use the regular expression matcher, this class can find and
return a set of results describing the matches found in the String. Obviously,
if any of the given info changes, those using this class need to perform the
search again to get the latest results.

@author	James Hoak
*/

public class StringFinder {

	/**
	Private constructor. Does nothing but protect the class from being
	constructed needlessly.
	*/
	private StringFinder() {}

	/**
	Searches a given String for occurrences of another String, and returns
	SearchResults that describe the matches found in the first String.
	@param toFind The String to look for
	@param toSearch The String to search through
	@param regex Whether or not toFind should be treated as a regular
	expression.
	@return An instance of SearchResults that describe the matches found.
	*/
	public static SearchResults search(String toFind, String toSearch, boolean regex) {
		LinkedList<IntPair> matchList = new LinkedList<>();

		if (regex) {
			// Find all matches of the given regex and put them in matchList.
			Matcher m = Pattern.compile(toFind).matcher(toSearch);
			while (!m.hitEnd()) {

				if (m.find()) {				// whether or not we found a match
					int start = m.start(),
						end = m.end();
					matchList.add(new IntPair(start, end));
				}
			}
		}
		else {
			// Find all matches of the given LITERAL and put them in matchList.
			int searchStartIndex = 0;
			int nextMatchStartIndex = toSearch.indexOf(toFind, 0);
			while (nextMatchStartIndex != -1) {
				// Add the match we found and move on to the next character
				// immediately after the match
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
