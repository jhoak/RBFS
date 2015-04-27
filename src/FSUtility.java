import java.io.*;

//This class helps with instances of reading input strings over a connection or from a file,
//where the end of a string is characterized by the ASCII character 13, the carriage return.
public class FSUtility {

	//This reads a string over a connection.
	public static String readConnectionInputToken(InputStreamReader rdr) throws IOException {

		int i = rdr.read();
		StringBuilder sb = new StringBuilder();
		while (i != 13) {
			sb.append((char)i);
			i = rdr.read();
		}
		return sb.toString();
	}

	//This reads a line from the info.dat file, given a number of characters to skip.
	//Example: For the line "Name: James Hoak", if I wanted to read "James Hoak"
	//         I'd skip 6 characters. (The cursor is assumed to start at the first letter.)
	//The end calls to fr.read() skip to the next line.
	public static String readFileInputToken(FileReader fr, int numToSkip) throws IOException {
		int i = -1;
		StringBuilder sb = new StringBuilder();

		for (int k = 0; k < numToSkip; k++)
			i = fr.read();

		while (i != 13) {
			sb.append((char)i);
			i = fr.read();
		}
		fr.read();
		fr.read(); //now on next line of file
		return sb.toString();
	}
}