package rbfs.protocol;

public class ServerSender {

	public enum Message {
		OK_LOGIN(0, "OK"),
		ERR_BAD_LOGIN(1, "BAD LOGIN"),
		ERR_NO_ROLES_FOUND(2, "NO ROLES FOUND"),
		OK_FILE_LIST(100, "OK"),
		ERR_NO_FILES_ACCESSIBLE(101, "NO FILES ACCESSIBLE"),
		OK_FILE_OPEN(200, "OK"),
		ERR_FILE_NOT_FOUND(201, "FILE NOT FOUND"),
		ERR_BAD_PERMISSIONS(202, "BAD PERMISSIONS"),
		OK_FILE_EDIT(300, "OK"),
		CLOSE(400, "CLOSE");

		private int code;
		private String text;

		private Message(int code, String text) {
			this.code = code;
			this.text = text;
		}

		public int code() {
			return code;
		}

		public String text() {
			return text;
		}
	}

	private static String simpleMessage(Message m) {
		return ProtocolUtils.pack(m.code(), m.text());
	}

	private static String complexMessage(Message m, String[] args) {
		return ProtocolUtils.pack(m.code(), m.text(), args);
	}

	public static String makeOKLoginMessage(String[] roles) {
		return complexMessage(Message.OK_LOGIN, roles);
	}

	public static String makeBadLoginMessage() {
		return simpleMessage(Message.ERR_BAD_LOGIN);
	}

	public static String makeNoRolesMessage() {
		return simpleMessage(Message.ERR_NO_ROLES_FOUND);
	}

	public static String makeFileListMessage() {
		// TODO: THIS!
	}

	public static String makeNoFilesMessage() {
		return simpleMessage(Message.ERR_NO_FILES_ACCESSIBLE);
	}

	public static String makeFileOpenMessage(String fileContents) {
		return complexMessage(Message.OK_FILE_OPEN, fileContents);
	}

	public static String makeFileNotFoundMessage() {
		return simpleMessage(Message.ERR_FILE_NOT_FOUND);
	}

	public static String makeBadPermissionsMessage() {
		return simpleMessage(Message.ERR_BAD_PERMISSIONS);
	}

	public static String makeFileEditMessage() {
		return simpleMessage(Message.OK_FILE_EDIT);
	}

	public static String makeCloseMessage() {
		return simpleMessage(Message.CLOSE);
	}

}