--Allows total removal of a role from the DB.
CREATE PROC RemoveRoleFromDB @userrole CHAR(30) AS
	DELETE FROM AccountsAndRoles WHERE role = @userrole;
	DELETE FROM Roles WHERE role = @userrole;
GO