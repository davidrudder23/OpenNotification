-- ======================================================================
-- ===   Sql Script for Database : reliable
-- ===
-- === Build : 12
-- ======================================================================

BEGIN WORK;
-- ======================================================================

CREATE TABLE member
  (
    uuid  varchar(64)   unique not null,
    type  int2          not null default 1,

    primary key(uuid)
  );

CREATE INDEX memberIDX1 ON member(uuid);

-- ======================================================================

CREATE TABLE group
  (
    uuid  varchar(64)   unique not null,
    type  int2          not null default 1,

    primary key(uuid),

    foreign key(uuid) references member(uuid) on update CASCADE on delete CASCADE
  );

CREATE INDEX groupIDX1 ON group(uuid);

-- ======================================================================

CREATE TABLE authentication
  (
    passphrase  varchar(255)   not null,
    user        varchar(64)    unique,
    userinfo    varchar(255)   unique,

    foreign key(user) references member(uuid) on update CASCADE on delete CASCADE
  );

-- ======================================================================

CREATE TABLE authorization
  (
    member  varchar(64)    not null,
    role    varchar(255)   not null,

    primary key(member),

    foreign key(member) references member(uuid) on update CASCADE on delete CASCADE
  );

CREATE INDEX authorizationIDX1 ON authorization(member);

-- ======================================================================

CREATE TABLE devicetype
  (
    uuid  varchar(64)    unique not null,
    name  varchar(255),

    primary key(uuid)
  );

-- ======================================================================

CREATE TABLE userinformation
  (
    user   varchar(64),
    name   varchar(255),
    value  varchar(255),

    foreign key(user) references member(uuid) on update CASCADE on delete CASCADE
  );

-- ======================================================================

CREATE TABLE membership
  (
    child   varchar(64),
    parent  varchar(64),
    uuid    varchar(64),

    foreign key(child) references member(uuid),
    foreign key(parent) references group(uuid)
  );

-- ======================================================================

CREATE TABLE escalationgroup
  (
    membership      varchar(64)   not null,
    escalationtime  int2          not null default 15,
    order           int2          not null default 1,
    numattempts     int2          not null default 1,

    primary key(membership),

    foreign key(membership) references membership(uuid)
  );

CREATE INDEX escalationgroupIDX1 ON escalationgroup(membership);

-- ======================================================================

CREATE TABLE pagelog
  (
    uuid         varchar(64)   unique not null,
    sender       varchar(64),
    recipient    varchar(64)   not null,
    time         date,
    confirmedby  varchar(64),

    primary key(uuid),

    foreign key(sender) references member(uuid),
    foreign key(recipient) references member(uuid),
    foreign key(confirmedby) references member(uuid)
  );

-- ======================================================================

CREATE TABLE errorlog
  (
    uuid     varchar(64)     unique not null,
    level    int2,
    message  varchar(2048),
    time     date,

    primary key(uuid)
  );

CREATE INDEX errorlogIDX1 ON errorlog(uuid);

-- ======================================================================

CREATE TABLE loginlog
  (
    uuid       varchar(64)    unique not null,
    time       date           not null,
    succeeded  bool           not null,
    username   varchar(255)   not null,

    primary key(uuid)
  );

-- ======================================================================

CREATE TABLE device
  (
    type  varchar(64),
    user  varchar(64),

    foreign key(type) references devicetype(uuid),
    foreign key(user) references member(uuid)
  );

-- ======================================================================

COMMIT;
-- ======================================================================

