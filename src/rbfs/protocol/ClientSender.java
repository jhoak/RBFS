package rbfs.protocol;

public class ClientSender {

	public enum Message {
		LOGIN("LOGIN"),
		OPEN_FILE("OPENF"),
		SAVE_FILE("SAVEF"),
		FILE_LIST("FLIST"),
		CLOSE("CLOSE");

		private String text;

		private Message(String text) {
			this.text = text;
		}

		public String text() {
			return text;
		}
	}


	public static String makeLoginMessage(String name, String passwd) {
		return ProtocolUtils.pack(LOGIN.text(), name, passwd);
	}

	public static String makeOpenMessage(String filename) {
		return ProtocolUtils.pack(OPEN_FILE.text(), filename);
	}

	public static String makeSaveMessage(String filename, String newContents) {
		return ProtocolUtils.pack(SAVE_FILE.text(), filename, newContents);
	}

	public static String makeFileListMessage(String[] roles) {
		return ProtocolUtils.pack(FILE_LIST.text(), roles);
	}

	public static String makeCloseMessage() {
		return ProtocolUtils.pack(CLOSE.text());
	}
}