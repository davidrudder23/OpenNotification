-- ======================================================================
-- ===   Sql Script for Database : Paging3
-- ===
-- === Build : 208
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

-- ======================================================================

CREATE TABLE errorlog
  (
    uuid        varchar(64),
    errorlevel  int,
    message     varchar(255),
    errortime   date,

    primary key(uuid)
  );

-- ======================================================================

CREATE TABLE loginlog
  (
    uuid                varchar(64),
    time                date           not null,
    succeeded           char(1)        not null,
    username            varchar(255)   not null,
    originatingaddress  varchar(255)
  );

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

-- ======================================================================

CREATE TABLE membergroup
  (
    uuid        varchar(64),
    membertype  int            not null,
    category    varchar(255),

    primary key(uuid),

    foreign key(uuid) references member(uuid) on delete CASCADE
  );

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

-- ======================================================================

CREATE TABLE notification
  (
    uuid                  varchar(64),
    recipient             varchar(64)    not null,
    time                  timestamp,
    confirmedby           varchar(64),
    subject               varchar(255),
    requiresconfirmation  bool,
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

-- ======================================================================

CREATE TABLE notificationmessages
  (
    notification  varchar(64),
    message       LONGBLOB,
    addedby       varchar(255),
    addedon       timestamp,
    contenttype   varchar(255)   default 'application/octet-stream',
    filename      varchar(255),

    foreign key(notification) references notification(uuid) on delete CASCADE
  );

-- ======================================================================

CREATE TABLE notificationoptions
  (
    notification  varchar(64)    not null,
    optionname    varchar(255)   not null,

    foreign key(notification) references notification(uuid) on delete CASCADE
  );

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

-- ======================================================================

CREATE TABLE notificationproviderinfo
  (
    provider  varchar(64),
    name      varchar(255),
    value     varchar(255),

    foreign key(provider) references notificationprovider(uuid) on delete CASCADE
  );

-- ======================================================================

CREATE TABLE schedule
  (
    uuid  varchar(64),
    name  varchar(255),

    primary key(uuid)
  );

-- ======================================================================

CREATE TABLE userinformation
  (
    member  varchar(64)    not null,
    name    varchar(255),
    value   varchar(255),

    foreign key(member) references member(uuid) on delete CASCADE
  );

-- ======================================================================

CREATE TABLE uuid
  (
    generic       int   default 1,
    member        int   default 1,
    notification  int   default 1,
    device        int   default 1
  );

-- ======================================================================

CREATE TABLE command
  (
    uuid   varchar(64),
    class  varchar(255)   not null,

    primary key(uuid)
  );

-- ======================================================================

CREATE TABLE commandauthz
  (
    command  varchar(64),
    member   varchar(64),

    foreign key(command) references command(uuid) on delete CASCADE,
    foreign key(member) references member(uuid) on delete CASCADE
  );

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

-- ======================================================================

CREATE TABLE authorizationinfo
  (
    member  varchar(64),
    role    varchar(255)   not null,

    foreign key(member) references member(uuid) on delete CASCADE
  );

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

-- ======================================================================

CREATE TABLE devicesetting
  (
    device      varchar(64),
    name        varchar(255)   not null,
    value       varchar(255),
    isrequired  char(1),

    foreign key(device) references device(uuid) on delete CASCADE
  );

-- ======================================================================

CREATE TABLE escalationgroup
  (
    membership      varchar(64),
    escalationtime  int2          default 15 not null,
    numattempts     int2          not null,

    primary key(membership),

    foreign key(membership) references membership(uuid) on delete CASCADE
  );

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

-- ======================================================================

