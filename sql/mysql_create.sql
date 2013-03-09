USE reliable

/* Database Items */

 /************ AddTable: Authentication ***************/ 

/* Rebuild structure for table */
CREATE TABLE authentication
(
	passphrase VARCHAR(255) NOT NULL,
	user VARCHAR(64) NULL,
	userinfo VARCHAR(255) NOT NULL
);

/* Authentication: Primary Key */
ALTER TABLE authentication ADD CONSTRAINT pkAuthentication
	PRIMARY KEY (userinfo);

 /************ AddTable: Authorization ***************/ 

/* Rebuild structure for table */
CREATE TABLE authorization
(
	member VARCHAR(64) NOT NULL,
	role VARCHAR(255) NOT NULL
);


 /************ AddTable: Device ***************/ 

/* Rebuild structure for table */
CREATE TABLE device
(
	type VARCHAR(64) NOT NULL,
	user VARCHAR(64) NOT NULL
);

/* Device: Primary Key */
ALTER TABLE device ADD CONSTRAINT pkDevice
	PRIMARY KEY (user);

 /************ AddTable: DeviceType ***************/ 

/* Rebuild structure for table */
CREATE TABLE devicetype
(
	Name VARCHAR(255) NOT NULL,
	uuid VARCHAR(64) NOT NULL
);

/* DeviceType: Primary Key */
ALTER TABLE devicetype ADD CONSTRAINT pkDeviceType
	PRIMARY KEY (uuid);

 /************ AddTable: ErrorLog ***************/ 

/* Rebuild structure for table */
CREATE TABLE errorlog
(
	level INTEGER NOT NULL  DEFAULT 1,
	message BLOB NOT NULL,
	uuid VARCHAR(64) NOT NULL
);

/* ErrorLog: Primary Key */
ALTER TABLE errorlog ADD CONSTRAINT pkErrorLog
	PRIMARY KEY (uuid);

 /************ AddTable: EscalationGroup ***************/ 

/* Rebuild structure for table */
CREATE TABLE escalationgroup
(
	escalationtime INTEGER NOT NULL  DEFAULT 15,
	membership VARCHAR(64) NOT NULL,
	numattempts INTEGER NOT NULL  DEFAULT 1,
	order INTEGER NOT NULL
);

/* EscalationGroup: Primary Key */
ALTER TABLE escalationgroup ADD CONSTRAINT pkEscalationGroup
	PRIMARY KEY (membership);

 /************ AddTable: Group ***************/ 

/* Rebuild structure for table */
CREATE TABLE group
(
	type INTEGER NOT NULL  DEFAULT 1,
	uuid VARCHAR(64) NOT NULL
);

/* Group: Primary Key */
ALTER TABLE group ADD CONSTRAINT pkGroup
	PRIMARY KEY (uuid);

 /************ AddTable: LoginLog ***************/ 

/* Rebuild structure for table */
CREATE TABLE loginlog
(
	date DATE NOT NULL,
	loginname VARCHAR(255) NOT NULL,
	succeeded TINYINT(1) NOT NULL  DEFAULT 'true',
	user VARCHAR(64) NULL,
	uuid VARCHAR(64) NOT NULL
);

/* LoginLog: Primary Key */
ALTER TABLE loginlog ADD CONSTRAINT pkLoginLog
	PRIMARY KEY (uuid);

 /************ AddTable: Member ***************/ 

/* Rebuild structure for table */
CREATE TABLE member
(
	type INTEGER NOT NULL  DEFAULT 1,
	uuid VARCHAR(64) NOT NULL
);

/* Member: Primary Key */
ALTER TABLE member ADD CONSTRAINT pkMember
	PRIMARY KEY (uuid);

 /************ AddTable: Membership ***************/ 

/* Rebuild structure for table */
CREATE TABLE membership
(
	child VARCHAR(64) NOT NULL,
	group VARCHAR(64) NOT NULL,
	uuid VARCHAR(64) NOT NULL
);

 /************ AddTable: PageLog ***************/ 

/* Rebuild structure for table */
CREATE TABLE pagelog
(
	confirmed_by VARCHAR(64) NOT NULL,
	recipient VARCHAR(64) NOT NULL,
	sender VARCHAR(64) NULL,
	time DATE NOT NULL,
	uuid VARCHAR(64) NOT NULL
);

/* PageLog: Primary Key */
ALTER TABLE pagelog ADD CONSTRAINT pkPageLog
	PRIMARY KEY (uuid);

 /************ AddTable: User ***************/ 

/* Rebuild structure for table */
CREATE TABLE user
(
	uuid VARCHAR(64) NOT NULL
);

/* User: Primary Key */
ALTER TABLE user ADD CONSTRAINT pkUser
	PRIMARY KEY (uuid);

 /************ AddTable: UserInformation ***************/ 

/* Rebuild structure for table */
CREATE TABLE UserInformation
(
	Name VARCHAR(255) NOT NULL,
	uuid VARCHAR(64) NOT NULL,
	Value VARCHAR(255) NOT NULL
)

/* UserInformation: Primary Key */
ALTER TABLE UserInformation ADD CONSTRAINT pkUserInformation
	PRIMARY KEY (uuid);
