/*==============================================================*/
/* Database name:  paging                                       */
/* DBMS name:      MySQL 3.23                                   */
/* Created on:     5/22/2004 4:55:14 PM                         */
/*==============================================================*/

use paging;

/*==============================================================*/
/* Table: carrier                                               */
/*==============================================================*/
create table if not exists carrier
(
   carrier_id                     integer                        not null AUTO_INCREMENT,
   carrier_name                   varchar(50)                    not null,
   server_address                 varchar(150)                   not null,
   port                           integer                        not null,
   inter                          integer                        not null,
   protocol                       varchar(30)                    not null,
   device_prefix                  varchar(150),
   device_suffix                  varchar(150),
   max_chars                      integer                        not null,
   primary key (carrier_id)
   
);

