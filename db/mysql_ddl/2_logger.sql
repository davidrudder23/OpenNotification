/*==============================================================*/
/* Database name:  paging                                       */
/* DBMS name:      MySQL 3.23                                   */
/* Created on:     5/22/2004 4:55:14 PM                         */
/*==============================================================*/

use paging;

/*==============================================================*/
/* Table: logger                                                */
/*==============================================================*/
create table if not exists logger
(
   logger_id                      decimal(12)                    not null,
   user_data_id                   varchar(160)                   not null,
   log_data                       mediumtext                     not null,
   logdate                        datetime                       not null,
   level                          integer,
   subtype                        varchar(250),
   message                        mediumtext,
   protocol_status                mediumtext,
   primary key (logger_id)
   
);

