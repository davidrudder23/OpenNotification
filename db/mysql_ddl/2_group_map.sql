/*==============================================================*/
/* Database name:  paging                                       */
/* DBMS name:      MySQL 3.23                                   */
/* Created on:     5/22/2004 4:55:14 PM                         */
/*==============================================================*/

use paging;

/*==============================================================*/
/* Table: group_map                                             */
/*==============================================================*/
create table if not exists group_map
(
   map_id                         decimal(12)                    not null,
   group_id                       decimal(9)                     not null,
   user_data_id                   decimal(9)                     not null,
   escalation_order               integer,
   primary key (map_id),
   unique (group_id),
   unique (user_data_id)
);
