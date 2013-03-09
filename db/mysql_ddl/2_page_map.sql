/*==============================================================*/
/* Database name:  paging                                       */
/* DBMS name:      MySQL 3.23                                   */
/* Created on:     5/22/2004 4:55:14 PM                         */
/*==============================================================*/

use paging;

/*==============================================================*/
/* Table: page_map                                              */
/*==============================================================*/
create table if not exists page_map
(
   page_map_key                   decimal(12)                    not null,
   user_data_id                   varchar(160)                   not null,
   escalation_number              integer,
   group_id                       decimal(9)                     not null,
   page_id                        decimal(12)                    not null,
   primary key (page_map_key),
   unique (page_id),
   unique (user_data_id),
   unique (group_id)
);

