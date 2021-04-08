#!/bin/bash
source /var/jahia/logs_env
sed -i "s|#LOGS_FOLDER#|$LOGS_FOLDER|g;s|#LOG_MAX_DAYS#|$LOG_MAX_DAYS|g;s|#LOG_MAX_SIZE#|$LOG_MAX_SIZE|g" /etc/logrotate.d/jahia_logrotate
