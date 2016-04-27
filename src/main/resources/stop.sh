#!/usr/bin/env bash

CONFSYNC_PID=`ps auxww | grep com\.confsync\.Start | grep -v grep | awk '{print $2}'`

if [ "$CONFSYNC_PID" != "" ]; then
    kill -9 $CONFSYNC_PID
    echo "Killed process $CONFSYNC_PID"
    sleep 1
else
	echo "No process found for confsync running"
	exit 1
fi
