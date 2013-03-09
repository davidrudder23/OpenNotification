#!/bin/sh
echo "Send Nagiod args: $@" >> /tmp/nagios.out
/home/drig/workspace/Paging/c-client/axis/SendNagiosNotification $@
