--Allows removal of a role from a user.
CREATE PROC RemoveRoleFromUser @username CHAR(20), @userrole CHAR(30) AS
	DELETE FROM AccountsAndRoles WHERE username = @username AND role = @userrole;
GO