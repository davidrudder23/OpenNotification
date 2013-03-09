#!/bin/sh

echo -n "PostgreSQL Database? "
read INPUT
                                                                                
DATABASE=$INPUT
                                                                                
echo -n "PostgreSQL Login Name? "
read INPUT
                                                                                
NAME=$INPUT
                                                                                
echo -n "PostgreSQL Host? "
read INPUT
                                                                                
HOST=$INPUT


DIR=`dirname $0`
DIR=$DIR/..

for x in $DIR/bin $DIR/lib/*
do
	export CLASSPATH=$CLASSPATH:$x
done

psql -U $NAME -h $HOST $DATABASE < $DIR/sql/postgresql.sql

cd $DIR/jakarta-tomcat-5.0.28/webapps/notification
java net.reliableresponse.notification.util.InitializeDB
