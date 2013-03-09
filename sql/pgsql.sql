-- ======================================================================
-- ===   Sql Script for Database : Reliable Response Notification Database
-- ===
-- === Build : 131
-- ======================================================================

DROP TABLE qrtz_trigger_listeners CASCADE;
DROP TABLE qrtz_simple_triggers CASCADE;
DROP TABLE qrtz_cron_triggers CASCADE;
DROP TABLE qrtz_blob_triggers CASCADE;
DROP TABLE priorityschedule CASCADE;
DROP TABLE priority CASCADE;
DROP TABLE escalationlog CASCADE;
DROP TABLE escalationgroup CASCADE;
DROP TABLE devicesetting CASCADE;
DROP TABLE device CASCADE;
DROP TABLE authorizationinfo CASCADE;
DROP TABLE authentication CASCADE;
DROP TABLE userinformation CASCADE;
DROP TABLE schedule CASCADE;
DROP TABLE qrtz_triggers CASCADE;
DROP TABLE qrtz_scheduler_state CASCADE;
DROP TABLE qrtz_paused_trigger_grps CASCADE;
DROP TABLE qrtz_locks CASCADE;
DROP TABLE qrtz_job_listeners CASCADE;
DROP TABLE qrtz_job_details CASCADE;
DROP TABLE qrtz_fired_triggers CASCADE;
DROP TABLE qrtz_calendars CASCADE;
DROP TABLE notificationproviderinfo CASCADE;
DROP TABLE notificationprovider CASCADE;
DROP TABLE notificationoptions CASCADE;
DROP TABLE notificationmessages CASCADE;
DROP TABLE notification CASCADE;
DROP TABLE membership CASCADE;
DROP TABLE membergroup CASCADE;
DROP TABLE member CASCADE;
DROP TABLE loginlog CASCADE;
DROP TABLE errorlog CASCADE;
DROP TABLE devicetype CASCADE;

-- ======================================================================

CREATE TABLE devicetype
  (
    uuid       varchar(64),
    name       varchar(255),
    classname  varchar(255)   not null,

    primary key(uuid)
  );

-- ======================================================================

CREATE TABLE errorlog
  (
    uuid        varchar(64),
    errorlevel  int,
    message     varchar(255),
    time        date,

    primary key(uuid)
  );

-- ======================================================================

CREATE TABLE loginlog
  (
    uuid       varchar(64),
    time       date           not null,
    succeeded  char(1)        not null,
    username   varchar(255)   not null,

    primary key(uuid)
  );

-- ======================================================================

CREATE TABLE member
  (
    uuid         varchar(64),
    type         int            not null,
    firstname    varchar(255),
    lastname     varchar(255),
    email        varchar(255),
    description  varchar(255),

    primary key(uuid)
  );

-- ======================================================================

CREATE TABLE membergroup
  (
    uuid        varchar(64),
    membertype  int2           not null,
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

    primary key(uuid),

    foreign key(child) references member(uuid) on delete CASCADE,
    foreign key(parent) references membergroup(uuid) on delete CASCADE
  );

-- ======================================================================

CREATE TABLE notification
  (
    uuid                  varchar(64),
    sender                varchar(255),
    recipient             varchar(64)    not null,
    time                  timestamp,
    confirmedby           varchar(64),
    subject               varchar(255),
    requiresconfirmation  bool,
    status                varchar(255),
    parent                varchar(64),

    primary key(uuid),

    foreign key(recipient) references member(uuid) on delete CASCADE,
    foreign key(confirmedby) references member(uuid) on delete CASCADE,
    foreign key(parent) references notification(uuid) on delete CASCADE
  );

-- ======================================================================

CREATE TABLE notificationmessages
  (
    notification  varchar(64),
    message       BYTEA,
    addedby       varchar(255),
    addedon       date,

    foreign key(notification) references notification(uuid) on delete CASCADE
  );

-- ======================================================================

CREATE TABLE notificationoptions
  (
    notification  varchar(64)    not null,
    optionname    varchar(255)   not null,

    foreign key(notification) references notification(uuid)
  );

-- ======================================================================

CREATE TABLE notificationprovider
  (
    notification  varchar(64),
    uuid          varchar(64),
    classname     varchar(255),

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

CREATE TABLE qrtz_calendars
  (
    calendar_name  varchar(80),
    calendar       BYTEA         not null,

    primary key(calendar_name)
  );

-- ======================================================================

CREATE TABLE qrtz_fired_triggers
  (
    entry_id           varchar(95),
    trigger_name       varchar(80)   not null,
    trigger_group      varchar(80)   not null,
    is_volatile        varchar(1)    not null,
    instance_name      varchar(80)   not null,
    fired_time         bigint        not null,
    state              varchar(16)   not null,
    job_name           varchar(80),
    job_group          varchar(80),
    is_stateful        varchar(1),
    requests_recovery  varchar(1),

    primary key(entry_id)
  );

-- ======================================================================

CREATE TABLE qrtz_job_details
  (
    job_name           varchar(80),
    job_group          varchar(80),
    description        varchar(120),
    job_class_name     varchar(128)   not null,
    is_durable         varchar(1)     not null,
    is_volatile        varchar(1)     not null,
    is_stateful        varchar(1)     not null,
    requests_recovery  varchar(1)     not null,
    job_data           BYTEA,

    primary key(job_name,job_group)
  );

-- ======================================================================

CREATE TABLE qrtz_job_listeners
  (
    job_name      varchar(80),
    job_group     varchar(80),
    job_listener  varchar(80),

    primary key(job_name,job_group,job_listener),

    foreign key(job_name,job_group) references qrtz_job_details(job_name,job_group)
  );

-- ======================================================================

CREATE TABLE qrtz_locks
  (
    lock_name  varchar(40),

    primary key(lock_name)
  );

-- ======================================================================

CREATE TABLE qrtz_paused_trigger_grps
  (
    trigger_group  varchar(80),

    primary key(trigger_group)
  );

-- ======================================================================

CREATE TABLE qrtz_scheduler_state
  (
    instance_name      varchar(80),
    last_checkin_time  bigint        not null,
    checkin_interval   bigint        not null,
    recoverer          varchar(80),

    primary key(instance_name)
  );

-- ======================================================================

CREATE TABLE qrtz_triggers
  (
    trigger_name    varchar(80),
    trigger_group   varchar(80),
    job_name        varchar(80)    not null,
    job_group       varchar(80)    not null,
    is_volatile     varchar(1)     not null,
    description     varchar(120),
    next_fire_time  bigint,
    prev_fire_time  bigint,
    trigger_state   varchar(16)    not null,
    trigger_type    varchar(8)     not null,
    start_time      bigint         not null,
    end_time        bigint,
    calendar_name   varchar(80),
    misfire_instr   smallint,

    primary key(trigger_name,trigger_group),

    foreign key(job_name,job_group) references qrtz_job_details(job_name,job_group)
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

CREATE TABLE authentication
  (
    passphrase  varchar(255)   not null,
    member      varchar(64),
    userinfo    varchar(255)   unique,
    resetkey    varchar(64)    unique,
    resettime   timestamp,

    primary key(member),

    foreign key(member) references member(uuid) on delete CASCADE
  );

-- ======================================================================

CREATE TABLE authorizationinfo
  (
    member  varchar(64),
    role    varchar(255)   not null,

    primary key(member),

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

CREATE INDEX devicesettingIDX1 ON devicesetting(name);

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
    notification  varchar(64),
    memberto      varchar(64),
    memberfrom    varchar(64),
    passed        bool,

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

CREATE TABLE qrtz_blob_triggers
  (
    trigger_name   varchar(80),
    trigger_group  varchar(80),
    blob_data      BYTEA,

    primary key(trigger_name,trigger_group),

    foreign key(trigger_name,trigger_group) references qrtz_triggers(trigger_name,trigger_group)
  );

-- ======================================================================

CREATE TABLE qrtz_cron_triggers
  (
    trigger_name     varchar(80),
    trigger_group    varchar(80),
    cron_expression  varchar(80)   not null,
    time_zone_id     varchar(80),

    primary key(trigger_name,trigger_group),

    foreign key(trigger_name,trigger_group) references qrtz_triggers(trigger_name,trigger_group)
  );

-- ======================================================================

CREATE TABLE qrtz_simple_triggers
  (
    trigger_name     varchar(80),
    trigger_group    varchar(80),
    repeat_count     bigint        not null,
    repeat_interval  bigint        not null,
    times_triggered  bigint        not null,

    primary key(trigger_name,trigger_group),

    foreign key(trigger_name,trigger_group) references qrtz_triggers(trigger_name,trigger_group)
  );

-- ======================================================================

CREATE TABLE qrtz_trigger_listeners
  (
    trigger_name      varchar(80),
    trigger_group     varchar(80),
    trigger_listener  varchar(80),

    primary key(trigger_name,trigger_group,trigger_listener),

    foreign key(trigger_name,trigger_group) references qrtz_triggers(trigger_name,trigger_group)
  );

-- ======================================================================

