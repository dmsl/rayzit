#!/bin/bash

#Restart the service
kill $(ps aux | grep '[p]j.jar' | awk '{print $2}');
java -classpath ../../pj.jar edu.rit.pj.cluster.JobScheduler ../../scheduler.conf &
