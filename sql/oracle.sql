REM ======================================================================
REM ===   Sql Script for Database : Paging3
REM ===
REM === Build : 205
REM ======================================================================

DROP TABLE priorityschedule CASCADE CONSTRAINTS;
DROP TABLE priority CASCADE CONSTRAINTS;
DROP TABLE escalationlog CASCADE CONSTRAINTS;
DROP TABLE escalationgroup CASCADE CONSTRAINTS;
DROP TABLE devicesetting CASCADE CONSTRAINTS;
DROP TABLE device CASCADE CONSTRAINTS;
DROP TABLE authorizationinfo CASCADE CONSTRAINTS;
DROP TABLE authentication CASCADE CONSTRAINTS;
DROP TABLE couponsused CASCADE CONSTRAINTS;
DROP TABLE coupon CASCADE CONSTRAINTS;
DROP TABLE paymenthistory CASCADE CONSTRAINTS;
DROP TABLE oncallschedule CASCADE CONSTRAINTS;
DROP TABLE commandauthz CASCADE CONSTRAINTS;
DROP TABLE command CASCADE CONSTRAINTS;
DROP TABLE uuid CASCADE CONSTRAINTS;
DROP TABLE userinformation CASCADE CONSTRAINTS;
DROP TABLE schedule CASCADE CONSTRAINTS;
DROP TABLE notificationproviderinfo CASCADE CONSTRAINTS;
DROP TABLE notificationprovider CASCADE CONSTRAINTS;
DROP TABLE notificationoptions CASCADE CONSTRAINTS;
DROP TABLE notificationmessages CASCADE CONSTRAINTS;
DROP TABLE notification CASCADE CONSTRAINTS;
DROP TABLE membership CASCADE CONSTRAINTS;
DROP TABLE membergroup CASCADE CONSTRAINTS;
DROP TABLE member CASCADE CONSTRAINTS;
DROP TABLE loginlog CASCADE CONSTRAINTS;
DROP TABLE errorlog CASCADE CONSTRAINTS;
DROP TABLE devicetype CASCADE CONSTRAINTS;
DROP TABLE account CASCADE CONSTRAINTS;
DROP SEQUENCE device_uuid_seq;
DROP SEQUENCE notification_uuid_seq;
DROP SEQUENCE member_uuid_seq;
DROP SEQUENCE uuid_seq;

REM ======================================================================

CREATE SEQUENCE uuid_seq
    INCREMENT BY 1 MINVALUE 1 MAXVALUE 9999999 START WITH 1;

REM ======================================================================

CREATE SEQUENCE member_uuid_seq
    INCREMENT BY 1 MINVALUE 1 MAXVALUE 9999999 START WITH 1;

REM ======================================================================

CREATE SEQUENCE notification_uuid_seq
    INCREMENT BY 1 MINVALUE 1 MAXVALUE 9999999 START WITH 1;

REM ======================================================================

CREATE SEQUENCE device_uuid_seq
    INCREMENT BY 1 MINVALUE 1 MAXVALUE 9999999 START WITH 1;

REM ======================================================================

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

REM ======================================================================

CREATE TABLE devicetype
  (
    uuid       varchar(64),
    name       varchar(255),
    classname  varchar(255)   not null,
    enabled    char(1)        default 'Y',
    primary key(uuid)
  );

CREATE INDEX devicetypeIDX1 ON devicetype(uuid);

REM ======================================================================

CREATE TABLE errorlog
  (
    uuid        varchar(64),
    errorlevel  integer,
    message     varchar(255),
    errortime   date,
    primary key(uuid)
  );

CREATE INDEX errorlogIDX1 ON errorlog(uuid);

REM ======================================================================

CREATE TABLE loginlog
  (
    uuid                varchar(64),
    time                date           not null,
    succeeded           char(1)        not null,
    username            varchar(255)   not null,
    originatingaddress  varchar(255)
  );

CREATE INDEX loginlogIDX1 ON loginlog(uuid);

REM ======================================================================

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
    loopcount    integer,
    primary key(uuid)
  );

CREATE INDEX memberIDX1 ON member(uuid,firstname,lastname);

REM ======================================================================

CREATE TABLE membergroup
  (
    uuid        varchar(64),
    membertype  integer        not null,
    category    varchar(255),
    primary key(uuid),
    foreign key(uuid) references member(uuid) on delete CASCADE
  );

CREATE INDEX membergroupIDX1 ON membergroup(uuid);

REM ======================================================================

CREATE TABLE membership
  (
    child       varchar(64),
    parent      varchar(64),
    uuid        varchar(64),
    childorder  integer       not null,
    priority    integer,
    owner       varchar(1)    default 'N',
    primary key(uuid),
    foreign key(child) references member(uuid) on delete CASCADE,
    foreign key(parent) references membergroup(uuid) on delete CASCADE
  );

CREATE INDEX membershipIDX1 ON membership(parent,uuid);

REM ======================================================================

CREATE TABLE notification
  (
    uuid                  varchar(64),
    recipient             varchar(64)    not null,
    time                  timestamp,
    confirmedby           varchar(64),
    subject               varchar(255),
    requiresconfirmation  varchar2(1),
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

REM ======================================================================

CREATE TABLE notificationmessages
  (
    notification  varchar(64),
    message       BLOB,
    addedby       varchar(255),
    addedon       timestamp,
    contenttype   varchar(255)   default 'application/octet-stream',
    foreign key(notification) references notification(uuid) on delete CASCADE
  );

CREATE INDEX notificationmessagesIDX1 ON notificationmessages(notification);

REM ======================================================================

CREATE TABLE notificationoptions
  (
    notification  varchar(64)    not null,
    optionname    varchar(255)   not null,
    foreign key(notification) references notification(uuid) on delete CASCADE
  );

CREATE INDEX notificationoptionsIDX1 ON notificationoptions(notification);

REM ======================================================================

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

REM ======================================================================

CREATE TABLE notificationproviderinfo
  (
    provider  varchar(64),
    name      varchar(255),
    value     varchar(255),
    foreign key(provider) references notificationprovider(uuid) on delete CASCADE
  );

CREATE INDEX notificationproviderinfoIDX1 ON notificationproviderinfo(provider);

REM ======================================================================

CREATE TABLE schedule
  (
    uuid  varchar(64),
    name  varchar(255),
    primary key(uuid)
  );

CREATE INDEX scheduleIDX1 ON schedule(uuid);

REM ======================================================================

CREATE TABLE userinformation
  (
    member  varchar(64)    not null,
    name    varchar(255),
    value   varchar(255),
    foreign key(member) references member(uuid) on delete CASCADE
  );

CREATE INDEX userinformationIDX1 ON userinformation(member,name);

REM ======================================================================

CREATE TABLE uuid
  (
    generic       integer   default 1,
    member        integer   default 1,
    notification  integer   default 1,
    device        integer   default 1
  );

CREATE INDEX uuidIDX1 ON uuid(generic);

REM ======================================================================

CREATE TABLE command
  (
    uuid   varchar(64),
    class  varchar(255)   not null,
    primary key(uuid)
  );

CREATE INDEX commandIDX1 ON command(uuid);

REM ======================================================================

CREATE TABLE commandauthz
  (
    command  varchar(64),
    member   varchar(64),
    foreign key(command) references command(uuid) on delete CASCADE,
    foreign key(member) references member(uuid) on delete CASCADE
  );

CREATE INDEX commandauthzIDX1 ON commandauthz(member);

REM ======================================================================

CREATE TABLE oncallschedule
  (
    member      varchar(64),
    allday      char(1),
    fromdate    timestamp,
    todate      timestamp,
    repetition  integer       default 1,
    repcount    integer       default 1,
    foreign key(member) references membership(uuid) on update CASCADE on delete CASCADE
  );

CREATE INDEX oncallscheduleIDX1 ON oncallschedule(member);

REM ======================================================================

CREATE TABLE paymenthistory
  (
    member             varchar(64)    not null,
    paymentdate        timestamp,
    paymentsuccessful  char(1)        default 'Y' not null,
    message            varchar(255),
    primary key(member),
    foreign key(member) references member(uuid)
  );

REM ======================================================================

CREATE TABLE coupon
  (
    uuid        varchar(64),
    name        varchar(255),
    nummonths   integer        default 1,
    indefinite  char(1)        default 'N',
    percentoff  integer        default 100,
    startdate   timestamp,
    enddate     timestamp,
    numuses     integer        default -1,
    numused     integer,
    primary key(uuid)
  );

CREATE INDEX couponIDX1 ON coupon(uuid,name);

REM ======================================================================

CREATE TABLE couponsused
  (
    coupon   varchar(64),
    account  varchar(64),
    date     timestamp,
    foreign key(coupon) references coupon(uuid),
    foreign key(account) references account(uuid)
  );

REM ======================================================================

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

REM ======================================================================

CREATE TABLE authorizationinfo
  (
    member  varchar(64),
    role    varchar(255)   not null,
    foreign key(member) references member(uuid) on delete CASCADE
  );

CREATE INDEX authorizationinfoIDX1 ON authorizationinfo(member);

REM ======================================================================

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

REM ======================================================================

CREATE TABLE devicesetting
  (
    device      varchar(64),
    name        varchar(255)   not null,
    value       varchar(255),
    isrequired  char(1),
    foreign key(device) references device(uuid) on delete CASCADE
  );

CREATE INDEX devicesettingIDX1 ON devicesetting(device);

REM ======================================================================

CREATE TABLE escalationgroup
  (
    membership      varchar(64),
    escalationtime  integer       default 15 not null,
    numattempts     integer       not null,
    primary key(membership),
    foreign key(membership) references membership(uuid) on delete CASCADE
  );

CREATE INDEX escalationgroupIDX1 ON escalationgroup(membership);

REM ======================================================================

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

REM ======================================================================

CREATE TABLE priority
  (
    uuid            varchar(64),
    member          varchar(64),
    prioritynumber  integer,
    device          varchar(64),
    primary key(uuid),
    foreign key(member) references member(uuid) on delete CASCADE,
    foreign key(device) references device(uuid) on delete CASCADE
  );

CREATE INDEX priorityIDX1 ON priority(uuid,member,device);

REM ======================================================================

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

REM ======================================================================

