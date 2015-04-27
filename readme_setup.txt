*********************************************************
* Project Name: RBFS			        *
* Author: James Hoak			        *
* Date of release: 27 Apr. 2015 (Version 1.0.0)	        *
* github.com/jhoak/RBFS			        *
*********************************************************

This readme briefly describes how to start up the server and client, as well as how to add new files to be hosted by the database.

Note that currently the program only works correctly with files readable (legibly) in a text editor, e.g. .txt, .cfg, .dat, source codes and others. 
This server requires a MSSQL database to run. Make sure your database has a username/password login so the server can connect to the database with it.
Last note: The files directory currently provides 3 sample files for use in the DB. To remove these, simply delete them from the folder and delete their entries in info.dat.

Part 1: Setting up a new database
Open the SQL Server Management Studio on the database machine. Execute the stored procedures "SetupAccountsAndRoles.sql," "InitializeANR.sql," and "ExecProcedures.sql." This creates the necessary tables with a predefined, specific set of roles to be used on the server. (See Part 4 for further info.)

Part 2: Starting the server
Make sure your server classes are in the classpath, as well as the FSUtility class. Also make sure your classpath links to sqljdbc4.jar. To run, go to cmd and type "java FileServer" without quotes. This will begin a setup process for the server, after which the server will be up and running. If you have problems connecting to the DB, make sure the server's DB account has the appropriate permissions, and make sure the DB's name and port are correct.

Part 3: Running the client
Go to cmd and type "java FileClient" without quotes. The client should start up and ask for a username/password combination. If you are connecting to a remote file server, click "Connect to..." and enter the IPv4 address of the server. Clicking "Login" with a valid username and password will allow you to begin viewing files hosted by the server.

Part 4: Modifying the database (when necessary)
There are multiple stored procedures provided to assist with modifying the database. Currently, one predefined set of roles is supported, and this is implemented specifically by the client and server code. (Later versions WILL change this.) The modifying procedures are located in the project folder under "Stored Procedures/Editing" and provide the exact functions their names imply.

Part 5: Adding new files to the database, and modifying file info for current ones.
To add a file to the server, on the server computer, do the following steps:
1) Locate the file you wish to add. For compatibility reasons, this must be either a text file, or at least a file displayable in a text editor, e.g. Notepad.
2) Add this file to the "Files" folder, located in the project directory.
3) Open info.dat in the project directory, and add the following seven lines, with the proper input in each set of angle brackets (<>):
***Note there are no spaces between the colons (:) and the file information. 
     E.g. Name:Filename.ext    <--- Legal
            Name: Filename.ext   <--- Illegal

Name:<File Name.extension>
Date created:<Date Created>
Date modified:<Date Modified>
Author:<Author of file>
Size:<Size in kilobytes> KB
Readable by:<role1,role2,...>
Writable by:<role1,role2,...>

4) Following the format of the info.dat file, put exactly one empty line between these seven lines and the last entry in the file.

5) Your file should now be displayable by the server.
If you are not the server administrator but would like to add a file to the server, ask your administrator to perform this task.


If you wish to simply modify permissions for the current set of hosted files, simply open info.dat in the project folder and change these permissions to the roles desired.
NOTE: Names do NOT work, in accordance with RBAC principles. 