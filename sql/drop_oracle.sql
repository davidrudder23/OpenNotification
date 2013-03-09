REM ======================================================================
REM ===   Sql Script for Database : Reliable Response Notification Database
REM ===
REM === Build : 153
REM ======================================================================

DROP TABLE qrtz_trigger_listeners CASCADE CONSTRAINTS;
DROP TABLE qrtz_simple_triggers CASCADE CONSTRAINTS;
DROP TABLE qrtz_cron_triggers CASCADE CONSTRAINTS;
DROP TABLE qrtz_blob_triggers CASCADE CONSTRAINTS;
DROP TABLE priorityschedule CASCADE CONSTRAINTS;
DROP TABLE priority CASCADE CONSTRAINTS;
DROP TABLE escalationlog CASCADE CONSTRAINTS;
DROP TABLE escalationgroup CASCADE CONSTRAINTS;
DROP TABLE devicesetting CASCADE CONSTRAINTS;
DROP TABLE device CASCADE CONSTRAINTS;
DROP TABLE authorizationinfo CASCADE CONSTRAINTS;
DROP TABLE authentication CASCADE CONSTRAINTS;
DROP TABLE uuid CASCADE CONSTRAINTS;
DROP TABLE userinformation CASCADE CONSTRAINTS;
DROP TABLE schedule CASCADE CONSTRAINTS;
DROP TABLE qrtz_triggers CASCADE CONSTRAINTS;
DROP TABLE qrtz_scheduler_state CASCADE CONSTRAINTS;
DROP TABLE qrtz_paused_trigger_grps CASCADE CONSTRAINTS;
DROP TABLE qrtz_locks CASCADE CONSTRAINTS;
DROP TABLE qrtz_job_listeners CASCADE CONSTRAINTS;
DROP TABLE qrtz_job_details CASCADE CONSTRAINTS;
DROP TABLE qrtz_fired_triggers CASCADE CONSTRAINTS;
DROP TABLE qrtz_calendars CASCADE CONSTRAINTS;
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
DROP SEQUENCE device_uuid_seq;
DROP SEQUENCE notification_uuid_seq;
DROP SEQUENCE member_uuid_seq;
DROP SEQUENCE uuid_seq;

