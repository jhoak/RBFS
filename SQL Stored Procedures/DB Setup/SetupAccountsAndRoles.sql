--Sets up accounts, roles, and accounts-and-roles tables in the DB.
CREATE PROC SetupAccountsAndRoles AS

	CREATE TABLE Accounts (
		username CHAR(20),
		password CHAR(20),
		firstname CHAR(20),
		lastname CHAR(20),
		PRIMARY KEY (username)
	);

	CREATE TABLE Roles (
		role CHAR(30),
		PRIMARY KEY (role),
	);

	CREATE TABLE AccountsAndRoles (
		username CHAR(20),
		role CHAR(30),
		PRIMARY KEY (username,role),
		FOREIGN KEY (username) REFERENCES Accounts,
		FOREIGN KEY (role) REFERENCES Roles
	);
GO