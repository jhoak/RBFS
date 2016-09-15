package rbfs.protocol;

public class ProtocolUtils {
	public static final int CLIENT_CMD_WIDTH = 5;

	public static final String SEPARATOR = "\r\r\r\r";

	public static String pack(String command, String... args) {
		// If separator is present in command or args, throw an exception.
		if (command.contains(SEPARATOR))
			throw new IllegalArgumentException("Bad command: " + command);
		for (String a : args) {
			if (a.contains(SEPARATOR))
				throw new IllegalArgumentException("Bad argument: " + a);
		}

		StringBuilder sb = new StringBuilder();
		sb.append(command + SEPARATOR);
		for (String s : args) {
			sb.append(s + SEPARATOR);
		}
		sb.append(SEPARATOR);
		return sb.toString();
	}

	public static String pack(int code, String command, String... args) {
		String message = pack(command, args);
		String wholeMessage = new String(code) + SEPARATOR + message;
		return wholeMessage;
	}

	public static String[] unpack(String message) {
		message = message.subString(0, message.length() - 2);
		return message.split(SEPARATOR);
	}
}