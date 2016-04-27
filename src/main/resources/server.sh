#!/usr/bin/env bash

CONFSYNC_PID=`ps auxww | grep com\.confsync\.Start | grep -v grep | awk '{print $2}'`
if [ "$CONFSYNC_PID" != "" ]; then
    echo "process $CONFSYNC_PID is running, please exec stop.sh to stop it first. "
	exit 0
fi

echo "starting confsync as server."

nohup java -classpath ".:lib/conf-sync-0.0.1-SNAPSHOT.jar:lib/gson-2.6.2.jar:lib/commons-lang3-3.4.jar:lib/commons-io-2.4.jar:lib/zookeeper-3.4.6.jar:lib/slf4j-api-1.6.1.jar:lib/slf4j-log4j12-1.6.1.jar:lib/log4j-1.2.16.jar:lib/jline-0.9.94.jar:lib/junit-3.8.1.jar:lib/netty-3.7.0.Final.jar:lib/zkclient-2.0.jar:lib/inieditor-r5.jar:lib/de.tototec.cmdoption-0.4.2.jar:lib/ant-1.9.4.jar:lib/ant-launcher-1.9.4.jar"  com.confsync.Start server $* 1>/dev/null 2>&1 &

sleep 2
CONFSYNC_PID=`ps auxww | grep com\.confsync\.Start | grep -v grep | awk '{print $2}'`
echo "confsync process $CONFSYNC_PID is running."

