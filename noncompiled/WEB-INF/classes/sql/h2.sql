-- ======================================================================
-- ===   Sql Script for Database : Paging3
-- ===
-- === Build : 206
-- ======================================================================

DROP TABLE IF EXISTS priorityschedule;
DROP TABLE IF EXISTS priority;
DROP TABLE IF EXISTS escalationlog;
DROP TABLE IF EXISTS escalationgroup;
DROP TABLE IF EXISTS devicesetting;
DROP TABLE IF EXISTS device;
DROP TABLE IF EXISTS authorizationinfo;
DROP TABLE IF EXISTS authentication;
DROP TABLE IF EXISTS couponsused;
DROP TABLE IF EXISTS coupon;
DROP TABLE IF EXISTS paymenthistory;
DROP TABLE IF EXISTS oncallschedule;
DROP TABLE IF EXISTS commandauthz;
DROP TABLE IF EXISTS command;
DROP TABLE IF EXISTS uuid;
DROP TABLE IF EXISTS userinformation;
DROP TABLE IF EXISTS schedule;
DROP TABLE IF EXISTS notificationproviderinfo;
DROP TABLE IF EXISTS notificationprovider;
DROP TABLE IF EXISTS notificationoptions;
DROP TABLE IF EXISTS notificationmessages;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS membership;
DROP TABLE IF EXISTS membergroup;
DROP TABLE IF EXISTS member;
DROP TABLE IF EXISTS loginlog;
DROP TABLE IF EXISTS errorlog;
DROP TABLE IF EXISTS devicetype;
DROP TABLE IF EXISTS account;
DROP SEQUENCE IF EXISTS device_UUID_SEQ;
DROP SEQUENCE IF EXISTS notification_UUID_SEQ;
DROP SEQUENCE IF EXISTS member_UUID_SEQ;
DROP SEQUENCE IF EXISTS UUID_SEQ;

-- ======================================================================

CREATE SEQUENCE UUID_SEQ
     START WITH 1 INCREMENT BY 1;

-- ======================================================================

CREATE SEQUENCE member_UUID_SEQ
     START WITH 1 INCREMENT BY 1;

-- ======================================================================

CREATE SEQUENCE notification_UUID_SEQ
     START WITH 1 INCREMENT BY 1;

-- ======================================================================

CREATE SEQUENCE device_UUID_SEQ
     START WITH 1 INCREMENT BY 1;

-- ======================================================================

CREATE TABLE account
  (
    uuid            varchar(64),
    payment_secret  varchar(255),
    lastpaid        timestamp,
    authorized      char(1)        default 'N',
    rate            float          default 40.00,
    phonerate       float,

    primary key(uuid)
  );

-- ======================================================================

CREATE TABLE devicetype
  (
    uuid       varchar(64),
    name       varchar(255),
    classname  varchar(255)   not null,
    enabled    char(1)        default 'Y',

    primary key(uuid)
  );

CREATE INDEX devicetypeIDX1 ON devicetype(uuid);

-- ======================================================================

CREATE TABLE errorlog
  (
    uuid        varchar(64),
    errorlevel  int,
    message     varchar(255),
    errortime   date,

    primary key(uuid)
  );

CREATE INDEX errorlogIDX1 ON errorlog(uuid);

-- ======================================================================

CREATE TABLE loginlog
  (
    uuid                varchar(64),
    time                date           not null,
    succeeded           char(1)        not null,
    username            varchar(255)   not null,
    originatingaddress  varchar(255)
  );

CREATE INDEX loginlogIDX1 ON loginlog(uuid);

-- ======================================================================

CREATE TABLE member
  (
    uuid         varchar(64),
    type         char(1)        not null,
    firstname    varchar(255),
    lastname     varchar(255),
    email        varchar(255),
    description  varchar(255),
    cached       char(1)        default 'N',
    deleted      char(1)        default 'N',
    deletedon    timestamp,
    vacation     char(1)        default 'N',
    loopcount    int,

    primary key(uuid)
  );

CREATE INDEX memberIDX1 ON member(uuid,firstname,lastname);

-- ======================================================================

CREATE TABLE membergroup
  (
    uuid        varchar(64),
    membertype  int            not null,
    category    varchar(255),

    primary key(uuid),

    foreign key(uuid) references member(uuid) on delete CASCADE
  );

CREATE INDEX membergroupIDX1 ON membergroup(uuid);

-- ======================================================================

CREATE TABLE membership
  (
    child       varchar(64),
    parent      varchar(64),
    uuid        varchar(64),
    childorder  int           not null,
    priority    int,
    owner       varchar(1)    default 'N',

    primary key(uuid),

    foreign key(child) references member(uuid) on delete CASCADE,
    foreign key(parent) references membergroup(uuid) on delete CASCADE
  );

CREATE INDEX membershipIDX1 ON membership(parent,uuid);

-- ======================================================================

CREATE TABLE notification
  (
    uuid                  varchar(64),
    recipient             varchar(64)    not null,
    time                  timestamp,
    confirmedby           varchar(64),
    subject               varchar(255),
    requiresconfirmation  boolean,
    status                varchar(255),
    parent                varchar(64),
    owner                 varchar(255),
    senderclass           varchar(255),
    senderinfo1           varchar(255),
    senderinfo2           varchar(255),
    senderinfo3           varchar(255),
    senderinfo4           varchar(255),
    senderinfo5           varchar(255),
    senderinfo6           varchar(255),
    senderinfo7           varchar(255),
    senderinfo8           varchar(255),
    senderinfo9           varchar(255),
    senderinfo10          varchar(255),

    primary key(uuid),

    foreign key(recipient) references member(uuid) on delete CASCADE,
    foreign key(confirmedby) references member(uuid) on delete CASCADE,
    foreign key(parent) references notification(uuid) on delete CASCADE
  );

CREATE INDEX notificationIDX1 ON notification(uuid,recipient,time,status);

-- ======================================================================

CREATE TABLE notificationmessages
  (
    notification  varchar(64),
    message       BYTEA,
    addedby       varchar(255),
    addedon       timestamp,
    contenttype   varchar(255)   default 'application/octet-stream',
    filename      varchar(255),

    foreign key(notification) references notification(uuid) on delete CASCADE
  );

CREATE INDEX notificationmessagesIDX1 ON notificationmessages(notification);

-- ======================================================================

CREATE TABLE notificationoptions
  (
    notification  varchar(64)    not null,
    optionname    varchar(255)   not null,

    foreign key(notification) references notification(uuid) on delete CASCADE
  );

CREATE INDEX notificationoptionsIDX1 ON notificationoptions(notification);

-- ======================================================================

CREATE TABLE notificationprovider
  (
    notification  varchar(64),
    uuid          varchar(64),
    classname     varchar(255),
    status        varchar(255),

    primary key(uuid),

    foreign key(notification) references notification(uuid) on delete CASCADE
  );

CREATE INDEX notificationproviderIDX1 ON notificationprovider(notification,uuid);

-- ======================================================================

CREATE TABLE notificationproviderinfo
  (
    provider  varchar(64),
    name      varchar(255),
    value     varchar(255),

    foreign key(provider) references notificationprovider(uuid) on delete CASCADE
  );

CREATE INDEX notificationproviderinfoIDX1 ON notificationproviderinfo(provider);

-- ======================================================================

CREATE TABLE schedule
  (
    uuid  varchar(64),
    name  varchar(255),

    primary key(uuid)
  );

CREATE INDEX scheduleIDX1 ON schedule(uuid);

-- ======================================================================

CREATE TABLE userinformation
  (
    member  varchar(64)    not null,
    name    varchar(255),
    value   varchar(255),

    foreign key(member) references member(uuid) on delete CASCADE
  );

CREATE INDEX userinformationIDX1 ON userinformation(member,name);

-- ======================================================================

CREATE TABLE uuid
  (
    generic       int   default 1,
    member        int   default 1,
    notification  int   default 1,
    device        int   default 1
  );

CREATE INDEX uuidIDX1 ON uuid(generic);

-- ======================================================================

CREATE TABLE command
  (
    uuid   varchar(64),
    class  varchar(255)   not null,

    primary key(uuid)
  );

CREATE INDEX commandIDX1 ON command(uuid);

-- ======================================================================

CREATE TABLE commandauthz
  (
    command  varchar(64),
    member   varchar(64),

    foreign key(command) references command(uuid) on delete CASCADE,
    foreign key(member) references member(uuid) on delete CASCADE
  );

CREATE INDEX commandauthzIDX1 ON commandauthz(member);

-- ======================================================================

CREATE TABLE oncallschedule
  (
    member      varchar(64),
    allday      char(1),
    fromdate    timestamp,
    todate      timestamp,
    repetition  int           default 1,
    repcount    int           default 1,

    foreign key(member) references membership(uuid) on update CASCADE on delete CASCADE
  );

CREATE INDEX oncallscheduleIDX1 ON oncallschedule(member);

-- ======================================================================

CREATE TABLE paymenthistory
  (
    member             varchar(64)    not null,
    paymentdate        timestamp,
    paymentsuccessful  char(1)        default 'Y' not null,
    message            varchar(255),

    primary key(member),

    foreign key(member) references member(uuid)
  );

-- ======================================================================

CREATE TABLE coupon
  (
    uuid        varchar(64),
    name        varchar(255),
    nummonths   int            default 1,
    indefinite  char(1)        default 'N',
    percentoff  int            default 100,
    startdate   timestamp,
    enddate     timestamp,
    numuses     int            default -1,
    numused     int,

    primary key(uuid)
  );

CREATE INDEX couponIDX1 ON coupon(uuid,name);

-- ======================================================================

CREATE TABLE couponsused
  (
    coupon   varchar(64),
    account  varchar(64),
    date     timestamp,

    foreign key(coupon) references coupon(uuid),
    foreign key(account) references account(uuid)
  );

-- ======================================================================

CREATE TABLE authentication
  (
    passphrase  varchar(255)   not null,
    member      varchar(64),
    userinfo    varchar(255)   unique,
    resetkey    varchar(64)    unique,
    resettime   timestamp,
    account     varchar(64),

    primary key(member),

    foreign key(member) references member(uuid) on delete CASCADE,
    foreign key(account) references account(uuid)
  );

CREATE INDEX authenticationIDX1 ON authentication(userinfo);

-- ======================================================================

CREATE TABLE authorizationinfo
  (
    member  varchar(64),
    role    varchar(255)   not null,

    foreign key(member) references member(uuid) on delete CASCADE
  );

CREATE INDEX authorizationinfoIDX1 ON authorizationinfo(member);

-- ======================================================================

CREATE TABLE device
  (
    type    varchar(64)   not null,
    member  varchar(64),
    uuid    varchar(64),

    primary key(uuid),

    foreign key(type) references devicetype(uuid) on delete CASCADE,
    foreign key(member) references member(uuid) on delete CASCADE
  );

CREATE INDEX deviceIDX1 ON device(member);

-- ======================================================================

CREATE TABLE devicesetting
  (
    device      varchar(64),
    name        varchar(255)   not null,
    value       varchar(255),
    isrequired  char(1),

    foreign key(device) references device(uuid) on delete CASCADE
  );

CREATE INDEX devicesettingIDX1 ON devicesetting(device);

-- ======================================================================

CREATE TABLE escalationgroup
  (
    membership      varchar(64),
    escalationtime  int2          default 15 not null,
    numattempts     int2          not null,

    primary key(membership),

    foreign key(membership) references membership(uuid) on delete CASCADE
  );

CREATE INDEX escalationgroupIDX1 ON escalationgroup(membership);

-- ======================================================================

CREATE TABLE escalationlog
  (
    notification    varchar(64),
    memberto        varchar(64),
    memberfrom      varchar(64),
    passed          char(1),
    escalationtime  timestamp,

    foreign key(notification) references notification(uuid) on delete CASCADE,
    foreign key(memberto) references member(uuid) on delete CASCADE,
    foreign key(memberfrom) references member(uuid) on delete CASCADE
  );

CREATE INDEX escalationlogIDX1 ON escalationlog(notification);

-- ======================================================================

CREATE TABLE priority
  (
    uuid            varchar(64),
    member          varchar(64),
    prioritynumber  int,
    device          varchar(64),

    primary key(uuid),

    foreign key(member) references member(uuid) on delete CASCADE,
    foreign key(device) references device(uuid) on delete CASCADE
  );

CREATE INDEX priorityIDX1 ON priority(uuid,member,device);

-- ======================================================================

CREATE TABLE priorityschedule
  (
    priority  varchar(64),
    schedule  varchar(64),
    uuid      varchar(64),

    primary key(uuid),

    foreign key(priority) references priority(uuid) on delete CASCADE,
    foreign key(schedule) references schedule(uuid) on delete CASCADE
  );

CREATE INDEX priorityscheduleIDX1 ON priorityschedule(priority,uuid);

-- ======================================================================

