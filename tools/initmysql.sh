#!/bin/sh

DIR=`dirname $0`
DIR=$DIR/..

for x in $DIR/bin $DIR/lib/*
do
	export CLASSPATH=$CLASSPATH:$x
done

mysqladmin --user=reliable --host=localhost --password=reliable -f drop reliable && mysqladmin --user=reliable --host=localhost --password=reliable create reliable && mysql  --user=reliable --host=localhost --password=reliable reliable < $DIR/sql/mysql.sql

java -Dorg.quartz.properties=$DIR/conf/quartz.properties net.reliableresponse.notification.util.InitializeDB
