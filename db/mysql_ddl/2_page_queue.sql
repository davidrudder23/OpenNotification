/*==============================================================*/
/* Database name:  paging                                       */
/* DBMS name:      MySQL 3.23                                   */
/* Created on:     5/22/2004 4:55:14 PM                         */
/*==============================================================*/

use paging;

/*==============================================================*/
/* Table: page_queue                                            */
/*==============================================================*/
create table if not exists page_queue
(
   page_id                        decimal(12)                    not null,
   page_status                    varchar(20)                    not null,
   sent_date                      datetime,
   message                        mediumtext                     not null,
   sender                         varchar(250),
   escalation_number              integer,
   primary key (page_id)
   
);
