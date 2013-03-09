#!/bin/sh

DIR=`dirname $0`
DIR=$DIR/..

echo -n "MySQL Database? "
read INPUT

DATABASE=$INPUT

echo -n "MySQL Login Name? "
read INPUT
                                                                                
NAME=$INPUT

echo -n "MySQL Host? "
read INPUT
                                                                                
HOST=$INPUT


for x in $DIR/bin $DIR/lib/*
do
	export CLASSPATH=$CLASSPATH:$x
done

mysqladmin --user=$NAME --host=$HOST -p drop $DATABASE
mysqladmin --user=$NAME --host=$HOST -p create $DATABASE
mysql --user=$NAME --host=$HOST -p $DATABASE < $DIR/sql/mysql.sql

cd $DIR/jakarta-tomcat-5.0.28/webapps/notification
java net.reliableresponse.notification.util.InitializeDB
