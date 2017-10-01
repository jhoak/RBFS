-- using SQLite syntax and datatypes --
create table User (
	uid integer,
	name text,
	pwd text,
	email text,
	primary key (uid)
);

create table Session (
	uid integer,
	skey text,
	primary key (skey),
	foreign key (uid) references User
);

create table Role (
	rid integer,
	rname text,
	perm_file text,
	primary key (rid)
);

create table HasRole (
	uid integer,
	rid integer,
	primary key (uid, rid),
	foreign key (uid) references User,
	foreign key (rid) references Role
);
