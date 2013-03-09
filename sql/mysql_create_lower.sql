use reliable

/* database items */

 /************ addtable: authentication ***************/ 

/* rebuild structure for table */
create table authentication
(
	passphrase varchar(255) not null,
	user varchar(64) null,
	userinfo varchar(255) not null
);

/* authentication: primary key */
alter table authentication add constraint pkauthentication
	primary key (userinfo);

 /************ addtable: authorization ***************/ 

/* rebuild structure for table */
create table authorization
(
	member varchar(64) not null,
	role varchar(255) not null
);


 /************ addtable: device ***************/ 

/* rebuild structure for table */
create table device
(
	devicetype varchar(64) not null,
	user varchar(64) not null
);

/* device: primary key */
alter table device add constraint pkdevice
	primary key (user);

 /************ addtable: devicetype ***************/ 

/* rebuild structure for table */
create table devicetype
(
	name varchar(255) not null,
	uuid varchar(64) not null
);

/* devicetype: primary key */
alter table devicetype add constraint pkdevicetype
	primary key (uuid);

 /************ addtable: errorlog ***************/ 

/* rebuild structure for table */
create table errorlog
(
	message blob not null,
	uuid varchar(64) not null
);

/* errorlog: primary key */
alter table errorlog add constraint pkerrorlog
	primary key (uuid);

 /************ addtable: escalationgroup ***************/ 

/* rebuild structure for table */
create table escalationgroup
(
	escalationtime integer not null  default 15,
	membership varchar(64) not null,
	numattempts integer not null  default 1,
	escorder integer not null
);

/* escalationgroup: primary key */
alter table escalationgroup add constraint pkescalationgroup
	primary key (membership);

 /************ addtable: group ***************/ 

/* rebuild structure for table */
create table group
(
	uuid varchar(64) not null
);

/* group: primary key */
alter table group add constraint pkgroup
	primary key (uuid);

 /************ addtable: loginlog ***************/ 

/* rebuild structure for table */
create table loginlog
(
	date date not null,
	loginname varchar(255) not null,
	succeeded tinyint(1) not null  default 'true',
	user varchar(64) null,
	uuid varchar(64) not null
);

/* loginlog: primary key */
alter table loginlog add constraint pkloginlog
	primary key (uuid);

 /************ addtable: member ***************/ 

/* rebuild structure for table */
create table member
(
	membertype integer not null  default 1,
	uuid varchar(64) not null
);

/* member: primary key */
alter table member add constraint pkmember
	primary key (uuid);

 /************ addtable: membership ***************/ 

/* rebuild structure for table */
create table membership
(
	child varchar(64) not null,
	group varchar(64) not null,
	uuid varchar(64) not null
);

 /************ addtable: pagelog ***************/ 

/* rebuild structure for table */
create table pagelog
(
	confirmed_by varchar(64) not null,
	recipient varchar(64) not null,
	sender varchar(64) null,
	time date not null,
	uuid varchar(64) not null
);

/* pagelog: primary key */
alter table pagelog add constraint pkpagelog
	primary key (uuid);

 /************ addtable: user ***************/ 

/* rebuild structure for table */
create table user
(
	uuid varchar(64) not null
);

/* user: primary key */
alter table user add constraint pkuser
	primary key (uuid);

 /************ addtable: userinformation ***************/ 

/* rebuild structure for table */
create table userinformation
(
	name varchar(255) not null,
	uuid varchar(64) not null,
	value varchar(255) not null
)

/* userinformation: primary key */
alter table userinformation add constraint pkuserinformation
	primary key (uuid);
