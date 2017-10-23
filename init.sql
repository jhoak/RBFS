-- using SQLite syntax and datatypes --
create table User (
	uid integer not null,
	name text not null,
	pwd text not null,
	email text not null,
	primary key (uid)
);

create table Session (
	uid integer not null,
	skey text not null,
	primary key (skey),
	foreign key (uid) references User
);

create table Role (
	rid integer not null,
	rname text not null,
	perm_file text not null,
	primary key (rid)
);

create table HasRole (
	uid integer not null,
	rid integer not null,
	primary key (uid, rid),
	foreign key (uid) references User,
	foreign key (rid) references Role
);
