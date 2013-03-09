/*==============================================================*/
/* Database name:  paging                                       */
/* DBMS name:      MySQL 3.23                                   */
/* Created on:     5/22/2004 4:55:14 PM                         */
/*==============================================================*/

use paging;

/*==============================================================*/
/* Table: contact_info                                          */
/*==============================================================*/
create table if not exists contact_info
(
   contact_type                   varchar(50)                    not null,
   contact_info                   varchar(254),
   contact_order                  integer                        not null,
   pager_num                      varchar(150)                   not null,
   pager_pin                      varchar(150),
   user_data_id                   varchar(160)                   not null,
   carrier_id                     integer                        not null,
   primary key (contact_type),
   unique (carrier_id)
   
);

