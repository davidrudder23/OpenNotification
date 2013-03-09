#!/bin/sh

DIR=`dirname $0`
DIR=$DIR/..

for x in $DIR/bin $DIR/lib/*
do
	export CLASSPATH=$CLASSPATH:$x
done

psql -U reliable -h localhost reliable < $DIR/sql/postgresql.sql

java -Dorg.quartz.properties=$DIR/conf/quartz.properties net.reliableresponse.notification.util.InitializeDB
