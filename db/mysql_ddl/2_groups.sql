/*==============================================================*/
/* Database name:  paging                                       */
/* DBMS name:      MySQL 3.23                                   */
/* Created on:     5/22/2004 4:55:14 PM                         */
/*==============================================================*/

use paging;

/*==============================================================*/
/* Table: groups                                               */
/*==============================================================*/
create table if not exists groups
(
   group_id                       decimal(9)                     not null,
   group_name                     varchar(250)                   not null,
   active                         bit                            not null,
   group_type_id                  decimal(9)                     not null,
   owner_id                       varchar(160),
   escalation                     bit,
   escalation_time                integer,
   rotation                       bit,
   primary key (group_id)
   
);

