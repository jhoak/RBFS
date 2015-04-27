--Allows adding a role into the DB. For a role to gain permissions, info.dat must be edited by the admin.
CREATE PROC AddRole @userrole CHAR(30) AS
	INSERT INTO Roles VALUES (@userrole);
GO