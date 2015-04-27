--Allows the addition of a new user to the DB.
CREATE PROC AddUser @username CHAR(20), @password CHAR(20), @first CHAR(20), @last CHAR(20), @initrole CHAR(30) AS
	IF EXISTS (SELECT * FROM Accounts WHERE username = @username)
		RAISERROR ('Error: Account name taken', 1, 10);
	IF NOT EXISTS (SELECT * FROM Roles WHERE role = @initrole)
		RAISERROR ('Error: Role not in database', 1, 10);
	
	INSERT INTO Accounts VALUES (@username,@password,@first,@last);
	INSERT INTO AccountsAndRoles VALUES (@username,@initrole);
GO