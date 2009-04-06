#!/bin/sh
killall -9 java
rm -rf /home/jahia/install/jahia
mkdir /home/jahia/install/jahia
tar xfz /home/jahia/install/*.tar.gz --directory /home/jahia/install
chmod 755 /home/jahia/install/jahia/bin/*.sh
export JAVA_HOME=/usr/java/latest
/home/jahia/install/jahia/bin/startup.sh
