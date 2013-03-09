CREATE TABLE loginlog (
  uuid VARCHAR(64) NOT NULL,
  time DATE NULL,
  succeeded TINYINT NULL,
  username VARCHAR(255) NULL,
  PRIMARY KEY(uuid),
  INDEX uuid(uuid)
);

CREATE TABLE escalationgroup (
  membership VARCHAR(64) NOT NULL,
  escalationtime SMALLINT NULL,
  numattempts SMALLINT NULL,
  PRIMARY KEY(membership),
  INDEX membership(membership),
  INDEX escalationgroupIDX1(membership)
);

CREATE TABLE member (
  uuid VARCHAR(64) NOT NULL,
  type SMALLINT NULL,
  firstname VARCHAR(255) NOT NULL,
  lastname VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  PRIMARY KEY(uuid),
  INDEX uuid(uuid),
  INDEX memberIDX1(uuid)
);

CREATE TABLE pageoptions (
  uuid VARCHAR(64) NOT NULL,
  optionname VARCHAR(255) NULL,
  PRIMARY KEY(uuid),
  INDEX uuid(uuid),
  INDEX pageoptionsIDX1(uuid)
);

CREATE TABLE page (
  uuid VARCHAR(64) NOT NULL,
  sender VARCHAR(255) NOT NULL,
  recipient VARCHAR(64) NULL,
  time DATE NOT NULL,
  confirmedby VARCHAR(64) NOT NULL,
  subject VARCHAR(255) NOT NULL,
  message BLOB NOT NULL,
  PRIMARY KEY(uuid),
  INDEX uuid(uuid)
);

CREATE TABLE errorlog (
  uuid VARCHAR(64) NOT NULL,
  level SMALLINT NOT NULL,
  message BLOB NOT NULL,
  time DATE NOT NULL,
  PRIMARY KEY(uuid),
  INDEX uuid(uuid),
  INDEX errorlogIDX1(uuid)
);

CREATE TABLE device (
  type VARCHAR(64) NULL,
  member VARCHAR(64) NOT NULL,
  uuid VARCHAR(64) NOT NULL,
  PRIMARY KEY(uuid),
  INDEX uuid(uuid),
  INDEX deviceIDX1(member)
);

CREATE TABLE devicesetting (
  device VARCHAR(64) NULL,
  name VARCHAR(255) NULL,
  value VARCHAR(255) NOT NULL,
  isrequired TINYINT NOT NULL,
  INDEX devicesettingIDX1(device, name)
);

CREATE TABLE devicetype (
  uuid VARCHAR(64) NOT NULL,
  name VARCHAR(255) NOT NULL,
  classname VARCHAR(255) NULL,
  PRIMARY KEY(uuid),
  INDEX uuid(uuid)
);

CREATE TABLE authentication (
  passphrase VARCHAR(255) NULL,
  member VARCHAR(64) NOT NULL,
  userinfo VARCHAR(255) NOT NULL,
  INDEX userinfo(userinfo),
  FOREIGN KEY(member)
    REFERENCES member(uuid)
      ON DELETE CASCADE
      ON UPDATE CASCADE
);

CREATE TABLE userinformation (
  member VARCHAR(64) NOT NULL,
  name VARCHAR(255) NOT NULL,
  value VARCHAR(255) NOT NULL
  FOREIGN KEY(member)
    REFERENCES member(uuid)
      ON DELETE CASCADE
      ON UPDATE NO ACTION
);

CREATE TABLE authorizationinfo (
  member VARCHAR(64) NOT NULL,
  role VARCHAR(255) NULL,
  PRIMARY KEY(member),
  INDEX authorizationinfoIDX1(member),
  FOREIGN KEY(member)
    REFERENCES member(uuid)
      ON DELETE CASCADE
      ON UPDATE NO ACTION
);

CREATE TABLE membergroup (
  uuid VARCHAR(64) NOT NULL,
  membertype SMALLINT NULL,
  PRIMARY KEY(uuid),
  INDEX uuid(uuid),
  INDEX membergroupIDX1(uuid),
  FOREIGN KEY(uuid)
    REFERENCES member(uuid)
      ON DELETE CASCADE
      ON UPDATE NO ACTION
);

CREATE TABLE membership (
  child VARCHAR(64) NOT NULL,
  childorder INTEGER NULL,
  parent VARCHAR(64) NOT NULL
  FOREIGN KEY(child)
    REFERENCES member(uuid)
      ON DELETE CASCADE
      ON UPDATE CASCADE,
  FOREIGN KEY(parent)
    REFERENCES membergroup(uuid)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
);


