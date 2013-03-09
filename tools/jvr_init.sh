#!/bin/sh
sudo /etc/init.d/dialogic stop
sudo /etc/init.d/dialogic start
export LD_PRELOAD=/usr/lib/libLiS.so.0.0.0
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/opt/src/jvr/lib
