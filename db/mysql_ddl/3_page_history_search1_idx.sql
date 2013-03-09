/*==============================================================*/
/* Database name:  paging                                       */
/* DBMS name:      MySQL 3.23                                   */
/* Created on:     5/22/2004 4:55:14 PM                         */
/*==============================================================*/

use paging;

/*==============================================================*/
/* Index: page_history_search1_idx                              */
/*==============================================================*/
create index page_history_search1_idx on page_history
(
   sent_date,
   confirm_date
);