/*==============================================================*/
/* Database name:  paging                                       */
/* DBMS name:      MySQL 3.23                                   */
/* Created on:     5/22/2004 4:55:14 PM                         */
/*==============================================================*/

use paging;

/*==============================================================*/
/* Table: user_data                                             */
/*==============================================================*/
create table if not exists user_data
(
   user_data_id                   varchar(160)                   not null,
   gro_user_data_id               decimal(9),
   fname                          varchar(20)                    not null,
   lname                          varchar(30)                    not null,
   user_type                      varchar(20)                    not null,
   user_perms_level               integer                        not null,
   email_addr                     varchar(150),
   passwd                         varchar(50)                    not null,
   primary key (user_data_id)
   
);
