import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.sql.*;
import java.util.Calendar;

//Allows clients to connect and read/write files that the server is hosting.
//Currently only supports files readable through a typical text editor. No pictures, videos, or class files.
public class FileServer {
	private static Connection cnxn;
	private static ServerSocket svrskt;
	private final static int port = 55554;
	private static Socket skt;
	private static StringBuilder sb;
	private static String workingDir = System.getProperty("user.dir");
	private final static File dir = new File(workingDir + "/Files");
	private final static File infoFile = new File(workingDir + "/info.dat");
	private static String username, password, dbName, portNum;

	private static String[] fileNames, datesCreated, datesModified, authors;
	private static int[] sizes;
	private static int[][] readable, writable;
	private static String[] roles = {"admin","IT guy","CEO","waterboy","sr software engineer","sick coder","accountant",
								"chief executive waterboy"};

	//Allows importing file information from info.dat.
	private static void importFileInfo() {
		try {
			FileReader fr = new FileReader(infoFile);
			int num = dir.listFiles().length;

			//Initialize arrays
			fileNames = new String[num];
			datesCreated = new String[num];
			datesModified = new String[num];
			authors = new String[num];
			sizes = new int[num];
			readable = new int[num][8];
			writable = new int[num][8];
			for (int x = 0; x < num; x++) {
				for (int y = 0; y < 8; y++) {
					readable[x][y] = 0;
					writable[x][y] = 0;
				}
			}

			//Set values
			int x = 0;
			int i = fr.read();
			StringBuilder sb = new StringBuilder();
			while (i != -1) {
				//Read name, date created, date last modified, author
				fileNames[x] = FSUtility.readFileInputToken(fr,5);
				datesCreated[x] = FSUtility.readFileInputToken(fr,13);
				datesModified[x] = FSUtility.readFileInputToken(fr,14);
				authors[x] = FSUtility.readFileInputToken(fr,7);

				//Read size
				for (int k = 0; k < 5; k++)
					i = fr.read();
				while (i != 32) {
					sb.append((char)i);
					i = fr.read();
				}
				sizes[x] = Integer.parseInt(sb.toString());
				sb = new StringBuilder();
				for (int k = 0; k < 5; k++)
					i = fr.read();


				//Read readable status
				String[] readRoles = new String[8];
				for (int k = 0; k < 12; k++)
					i = fr.read();
				int n = 0;
				while (i != 10) {
					while ((char)i != ',' && i != 13) {
						sb.append((char)i);
						i = fr.read();
					}
					i = fr.read();
					readRoles[n] = sb.toString();
					n++;
					sb = new StringBuilder();
				}
				i = fr.read();
				for (int k = 0; k < n; k++) {
					for (int l = 0; l < 8; l++) {
						if (readRoles[k].equals(roles[l]))
							readable[x][l] = 1;
					}
				}
				//All roles that can read the file are now shown in the array.

				//Now, read the writable status.
				String[] writeRoles = new String[8];
				for (int k = 0; k < 12; k++)
					i = fr.read();
				n = 0;
				while (i != 10 && i != -1) {
					while ((char)i != ',' && i != 13 && i != -1) {
						sb.append((char)i);
						i = fr.read();
					}
					i = fr.read();
					writeRoles[n] = sb.toString();
					n++;
					sb = new StringBuilder();
				}

				for (int k = 0; k < n; k++) {
					for (int l = 0; l < 8; l++) {
						if (writeRoles[k].equals(roles[l]))
							writable[x][l] = 1;
					}
				}

				fr.read();
				fr.read();
				i = fr.read();
				x++;

			}
			fr.close();

		}catch(IOException x) {
			System.out.println("Failed to import file info.");
		}
	}

	//Connects to the DB located on this machine.
	//Does not support connections to a remote DB.
	private static void connectToDB() {
		try {
			try {
				Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			}catch (Exception x) {System.out.println("Couldn't find the JDBC driver. Make sure it is in the project directory and try again.");}
			String url = "jdbc:sqlserver://" + dbName + ":" + portNum;
			cnxn = DriverManager.getConnection(url,username,password);
			System.out.println("Database connection established. Waiting for clients to connect...");
		}catch (SQLException s) {
			System.out.println(s.getMessage());
			return;
		}
	}

	//Pushes files to the client that are readable by user's active roles.
	private static void pushFiles(int[] roles, OutputStreamWriter writer) {
		//First, figure out the files readable by the client (and the total count).
		int[] filesReadable = new int[fileNames.length];
		int numFiles = 0;
		for (int i = 0; i < 8; i++) {
			if (roles[i] == 0) continue;
			for (int j = 0; j < fileNames.length; j++) {
				if (readable[j][i] == 1) {
					filesReadable[j] = 1;
					numFiles++;
				}
			}
		}
		//We now know all files readable by the client.
		//Check files writable.
		int[] filesWritable = new int[fileNames.length];
		for (int i = 0; i < 8; i++) {
			if (roles[i] == 0) continue;
			for (int j = 0; j < fileNames.length; j++) {
				if (writable[j][i] == 1) {
					filesWritable[j] = 1;
				}
			}
		}
		//First write the number of files.
		try {
			writer.write(numFiles + "" + (char)13);
			writer.flush();

			if (numFiles == 0) return;

			//Now write file contents for each readable file.
			for (int i = 0; i < fileNames.length; i++) {
				if (filesReadable[i] == 0) continue;
				writer.write(fileNames[i] + (char)13);
				writer.write(datesCreated[i] + (char)13);
				writer.write(datesModified[i] + (char)13);
				writer.write(authors[i] + (char)13);
				writer.write(sizes[i] + "" + (char)13);
				writer.write(((filesWritable[i] == 1) ? (char)1 : (char) 0) + "" + (char)13);
				writer.flush();
			}
		}catch (IOException e) {System.out.println("Failed to push files."); System.exit(0);}
	}

	//Sets up the server, waits for connections and takes input/serves output in the form of files and other data.
	public static void main(String[] args) {
		JOptionPane.showMessageDialog(null, "Welcome to the server setup! Before running this server, you'll need to " +
											"specify \nthe MSSQL database's name and port number as well as a " +
											"username\nand password for the server to use to connect. Hit OK to continue.");
		dbName = JOptionPane.showInputDialog("Enter the name of the database. (for instance, MyComputer"+"\\"+"SQLEXPRESS)");
		portNum = JOptionPane.showInputDialog("Enter the port number for the database. By default, SQLServer uses the port 1433.");
		username = JOptionPane.showInputDialog("Enter the username the server can use to connect to the database:");
		password = JOptionPane.showInputDialog("Enter the corresponding password:");
		//Import file info, and connect to DB.
		importFileInfo();
		connectToDB();

		//Listen for, and respond to, connections.
		try {
			svrskt = new ServerSocket(port);
			int i;

			while (true) {
				skt = svrskt.accept();
				BufferedInputStream input = new BufferedInputStream(skt.getInputStream());
				InputStreamReader rdr = new InputStreamReader(input);

				// Read username, PW
				String user = FSUtility.readConnectionInputToken(rdr);
				String pw = FSUtility.readConnectionInputToken(rdr);

				//Check user/PW combo against database. If it matches nothing, send ' ' then a string.
				// If it matches something and returns roles from the DB, get ready to send tons of output.

				Statement s = cnxn.createStatement();
				ResultSet set = s.executeQuery("SELECT role FROM Accounts A, AccountsAndRoles R " +
											   "WHERE A.username = '" + user + "' AND A.password = '" + pw + "' " +
											   "AND A.username = R.username");

				if (!set.next()) {
					//Bad username/PW combination. DB returns 0 roles, i.e. not in DB. So return.

					BufferedOutputStream output = new BufferedOutputStream(skt.getOutputStream());
					OutputStreamWriter writer = new OutputStreamWriter(output);
					writer.write((char)0 + "Error: Invalid ID/password combo." + (char)13);
					writer.flush();
					skt.close();
				}
				else {
					// Get roles.
					String[] rolelist = new String[8];
					rolelist[0] = set.getString(1).trim();
					int j = 1;
					while (set.next()) {
						rolelist[j] = set.getString(1).trim();
						j++;
					}
					//Obtained the list of roles for this person.

					//OUTPUT TIME #1.
					BufferedOutputStream output = new BufferedOutputStream(skt.getOutputStream());
					OutputStreamWriter writer = new OutputStreamWriter(output);
					writer.write((char)65);
					writer.flush();

					//Write number of roles
					writer.write(("" + j) + (char)13);
					writer.flush();

					String currentRole = null;
					int roleIndex = 0;
					int index = -1;
					int numFiles = 0;
					while (numFiles == 0 && roleIndex < rolelist.length) {
						//Find index of current role in master list of roles
						currentRole = rolelist[roleIndex];
						for (int k = 0; k < 8; k++) {
							if (currentRole.equals(roles[k])) {
								index = k;
							}
						}
						//Find how many files the current role can read
						for (int k = 0; k < fileNames.length; k++) {
							if (readable[k][index] == 1)
								numFiles++;
						}
						//If numFiles is still zero, try the next role.
						if (numFiles == 0)
							roleIndex++;
					}

					//Switch array so selected role is at front.
					String[] newArr = new String[j];
					for (int k = 0; k < j; k++) {
						int newPos = (k - roleIndex + j) % j;
						newArr[newPos] = rolelist[k];
					}
					rolelist = newArr;

					//Write role names to client.
					for (int k = 0; k < j; k++) {
						writer.write(rolelist[k] + "" + (char)13);
						writer.flush();
					}

					//Now, if no role of the user can read any files, just quit with an error message. Otherwise, keep going
					// with the current role.
					if (roleIndex == rolelist.length) {
						writer.write((char)0 + "Error: No files are accessible to the user." + (char)13);
						writer.flush();
						continue;
					}
					else {
						writer.write((char)1);
						writer.flush();
					}

					// Step 3) Send number of files to client.
					writer.write(String.valueOf(numFiles) + (char)13);
					writer.flush();


					// WRECK TIME: For each file readable with the chosen role, send the 6 pieces of info.
					for (int k = 0; k < fileNames.length; k++) {
						if (readable[k][index] == 1) {
							//Send filename
							writer.write(fileNames[k] + (char)13);
							//Send date created
							writer.write(datesCreated[k] + (char)13);
							//Send date modified
							writer.write(datesModified[k] + (char)13);
							//Send author
							writer.write(authors[k] + (char)13);
							//Send file size
							writer.write(String.valueOf(sizes[k]) + "" + (char)13);
							//Send writability
							writer.write(String.valueOf(writable[k][index] == 1 ? (char)1 : (char)0) + (char)13);
							writer.flush();
						}
					}
					while (true) {
						//Listen for requests from the client.
						String cmd = null;
						try {
							cmd = FSUtility.readConnectionInputToken(rdr);
						}catch (Exception e) {
							break;
						}
						if (cmd.equals("open")) {
							String file = FSUtility.readConnectionInputToken(rdr);

							//Send the file, wait for a push if user has write perm.
							try {
								FileReader fr = new FileReader(new File(dir.getName() + "/" + file));
								int m = fr.read();
								sb = new StringBuilder();
								while (m != -1) {
									sb.append((char)m);
									m = fr.read();
								}
								fr.close();
								writer.write(sb.toString() + (char)0);
								writer.flush();
							}catch (IOException x) {
								System.out.println(x.getMessage());
								System.out.println("Broke here.");
							}
						}
						else if (cmd.equals("save")) {
							//Get name of file we're trying to save
							String file = FSUtility.readConnectionInputToken(rdr);

							//Change file, as well as date modified and size in info.dat.
							// -Step 1: Get new file contents.
							int z = rdr.read();
							StringBuilder sb = new StringBuilder();
							while (z != 0) {
								sb.append((char)z);
								z = rdr.read();
							}
							String newtext = sb.toString();

							//Step 2: Save over previous file
							PrintWriter pwr = new PrintWriter(new File(dir + "/" + file));
							int textLen = newtext.length();
							int charIndex = 0;
							while (charIndex != textLen) {
								char c = newtext.charAt(charIndex);
								if (c == '\n')
									pwr.write((char)13 + "" + (char)10);
								else
									pwr.write(c);
								charIndex++;
							}
							pwr.close();

							//Step 3: Get date modified, file size
							Calendar c = Calendar.getInstance();
							String dateModified = (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.DAY_OF_MONTH) + "/" +
													(c.get(Calendar.YEAR)-2000);
							int fileSize = (int)Math.round(new File(file).length() / 1000.0); //file size in KB

							//Step 4: Save over info.dat entry
							StringBuilder newInfo = new StringBuilder();
							FileReader fr = new FileReader(infoFile);
							//Sub-step: advance until we're at the start of the entry for the given file
							newInfo.append((char)fr.read() +"ame:");
							String filename = FSUtility.readFileInputToken(fr, 5);
							newInfo.append(filename + (char)13 + (char)10);
							while (!filename.equals(file)) {
								//Starting on DateCreated line
								newInfo.append('D');
								int a = fr.read();
								while ((char)a != 'N') {
									newInfo.append((char)a);
									a = fr.read();
								}
								//Next read starts on next name line.
								filename = FSUtility.readFileInputToken(fr,5);
								newInfo.append("Name:"+filename + (char)13 + "" + (char)10);
							}
							//We are now at the entry for this file.
							//Read down to the dates modified line:
							newInfo.append("Date created:"+ FSUtility.readFileInputToken(fr,13) + (char)13 + "" + (char)10);

							//Read down to sizes line
							FSUtility.readFileInputToken(fr,14);
							newInfo.append("Date modified:"+ dateModified + (char)13 + "" + (char)10);
							newInfo.append("Author:"+ FSUtility.readFileInputToken(fr,7) + (char)13 + "" + (char)10 + "S");

							//Read rest of file from start of sizes line
							int a = fr.read();
							while (a != -1) {
								newInfo.append((char)a);
								a = fr.read();
							} //at end of file

							fr.close();
							pwr = new PrintWriter(infoFile);
							pwr.write(newInfo.toString());
							pwr.close();


							//Step 5: Enter into server info
							importFileInfo();

							//Done!
						}
						else if (cmd.equals("getfiles")) {
							//Read roles that user has sent
							String[] rolesGiven = new String[8];
							int numSent = 0;
							for (int k = 0; k < 8; k++) {
								String str = FSUtility.readConnectionInputToken(rdr);
								if (str.equals("end"))
									break;
								rolesGiven[k] = str;
								numSent++;
							}

							//We now have all roles that the user has selected.
							//Now send all files and info.

							int[] roleArr = new int[8];
							for (int k = 0; k < numSent; k++) {
								for (int l = 0; l < roleArr.length; l++) {
									if (rolesGiven[k].equals(roles[l])){
										roleArr[l] = 1;
									}
								}
							}
							pushFiles(roleArr, writer);
						}

					}
				}
			}

		}catch (Exception e) {
			try {
				e.printStackTrace();
				//System.out.println("Had to close connection.");
				skt.close();
			}catch (Exception x) { System.out.println("Closing failed...");}
		}
	}
}