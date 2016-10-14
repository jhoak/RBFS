package rbfs.protocol;

public class ProtocolUtils {
	public static final int CLIENT_CMD_MAX_WIDTH = 5;

	public static final String SEPARATOR = "\0";
	private static final String SEP = SEPARATOR;

	public static String pack(String command, String... args) {
		// If separator is present in command or args, throw an exception.
		if (command.contains(SEP))
			throw new IllegalArgumentException("Bad command: " + command);
		for (String a : args) {
			if (a.contains(SEP))
				throw new IllegalArgumentException("Bad argument: " + a);
		}

		StringBuilder sb = new StringBuilder();
		sb.append(command + SEP);
		for (String s : args) {
			sb.append(s + SEP);
		}
		sb.append(SEP);
		return sb.toString();
	}

	public static String pack(int code, String command, String... args) {
		String message = pack(command, args);
		String wholeMessage = new String(code) + SEP + message;
		return wholeMessage;
	}

	public static String[] unpack(String message) {
		message = message.subString(0, message.length() - 2);
		return message.split(SEP);
	}
}