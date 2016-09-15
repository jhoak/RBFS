package rbfs.protocol;

public class Receiver {
	
	public static String[] readMessage(String message) {
		return ProtocolUtils.unpack(message);
	}
}