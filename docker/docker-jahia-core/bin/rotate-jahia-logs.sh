#!/bin/bash

while :
do
  /usr/sbin/logrotate -s /var/log/jahia/rotate-status /usr/local/tomcat/conf/jahia_logrotate > /dev/null 2>&1

  current_epoch=$(date +%s)
  target_epoch=$(date --date="next day 00:00:00" +%s)
  sleep_seconds=$(( $target_epoch - $current_epoch ))
  echo $sleep_seconds
  sleep $sleep_seconds
done
