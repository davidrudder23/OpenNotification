#!/bin/sh

DIR=`dirname $0`
DIR=$DIR/..

for x in $DIR/bin $DIR/lib/*
do
	export CLASSPATH=$CLASSPATH:$x
done

export ORACLE_SID=reliable
sqlplus reliable/reliable@reliable @$DIR/sql/oracle.sql

java -Dorg.quartz.properties=$DIR/conf/quartz.properties net.reliableresponse.notification.util.InitializeDB
