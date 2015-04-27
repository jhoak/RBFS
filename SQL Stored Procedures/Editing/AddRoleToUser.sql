--Allows adding a role to a user if Static SOD constraints are met.
CREATE PROC AddRoleToUser @name CHAR(20), @userrole CHAR(30) AS
	IF NOT EXISTS (SELECT A.username FROM Accounts A WHERE A.username = @name)
		RAISERROR ('Error: user not in database', 1, 10);
	
	IF NOT EXISTS (SELECT R.role FROM Roles R WHERE R.role = @userrole)
		RAISERROR ('Error: role not in database', 1,10);
	
	IF ((@userrole = 'waterboy' AND EXISTS (SELECT * FROM AccountsAndRoles
										  WHERE @name = username AND role = 'chief executive waterboy'))
	    OR
	    (@userrole = 'chief executive waterboy' AND EXISTS (SELECT * FROM AccountsAndRoles
															WHERE @name = username AND role = 'waterboy')))
															
		RAISERROR ('Error: Cannot be both a waterboy and a chief executive waterboy', 1,10);
		
	IF ((@userrole = 'CEO' AND EXISTS (SELECT * FROM AccountsAndRoles
									   WHERE @name = username AND role = 'accountant'))
	    OR
	    (@userrole = 'accountant' AND EXISTS (SELECT * FROM AccountsAndRoles
											  WHERE @name = username AND role = 'CEO')))
															
		RAISERROR ('Error: Cannot be both a CEO and an accountant', 1,10);
		
	INSERT INTO AccountsAndRoles VALUES (@name, @userrole);	
GO