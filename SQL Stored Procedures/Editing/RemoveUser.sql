--Allows removal of a user from the DB.
CREATE PROC RemoveUser @username CHAR(20) AS
	DELETE FROM Accounts WHERE username = @username;
	DELETE FROM AccountsAndRoles WHERE username = @username;
GO