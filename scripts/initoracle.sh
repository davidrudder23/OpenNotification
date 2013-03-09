#!/bin/sh

DIR=`dirname $0`
DIR=$DIR/..

for x in $DIR/bin $DIR/lib/*
do
	export CLASSPATH=$CLASSPATH:$x
done

DIR=`dirname $0`
DIR=$DIR/..
 
echo -n "Oracle SID? "
read INPUT
 
DATABASE=$INPUT
 
echo -n "Oracle Login Name? "
read INPUT
                                                                                
NAME=$INPUT
 
echo -n "Oracle Home? "
read INPUT
                                                                                
HOME=$INPUT

export ORACLE_SID=$DATABASE
export ORACLE_HOME=$HOME
$HOME/bin/sqlplus reliable @$DIR/sql/oracle.sql

cd $DIR/jakarta-tomcat-5.0.28/webapps/notification
java net.reliableresponse.notification.util.InitializeDB
