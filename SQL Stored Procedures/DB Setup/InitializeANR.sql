--Sample initialization of users and roles in the DB.
CREATE PROC InitializeANR AS
	
	INSERT INTO Accounts VALUES ('jhoak',			'907865490786',	 'James',	 'Hoak');
	INSERT INTO Accounts VALUES ('gnewell',			'gaben',		 'Gabe',	 'Newell');
	INSERT INTO Accounts VALUES ('jliddy',			'assemblyisezm8','Jack',	 'Liddy');
	INSERT INTO Accounts VALUES ('tchen',			'uwotm8',		 'Ted',		 'Chen');
	INSERT INTO Accounts VALUES ('jferri',			'shrekt',		 'Justin',	 'Ferri');
	
	INSERT INTO Roles VALUES ('admin');
	INSERT INTO Roles VALUES ('IT guy');
	INSERT INTO Roles VALUES ('CEO');
	INSERT INTO Roles VALUES ('waterboy');
	INSERT INTO Roles VALUES ('sr software engineer');
	INSERT INTO Roles VALUES ('sick coder');
	INSERT INTO Roles VALUES ('accountant');
	INSERT INTO Roles VALUES ('chief executive waterboy');

	INSERT INTO AccountsAndRoles VALUES ('jhoak',			'admin');
	INSERT INTO AccountsAndRoles VALUES ('jhoak',			'IT guy');
	INSERT INTO AccountsAndRoles VALUES ('gnewell',			'CEO');
	INSERT INTO AccountsAndRoles VALUES ('jliddy',			'waterboy');
	INSERT INTO AccountsAndRoles VALUES ('jliddy',			'sr software engineer');
	INSERT INTO AccountsAndRoles VALUES ('tchen',			'sick coder');
	INSERT INTO AccountsAndRoles VALUES ('jferri',			'accountant');
	INSERT INTO AccountsAndRoles VALUES ('jferri',			'chief executive waterboy');

GO