#!/bin/sh
echo ---------------------------------------------------------------------------
echo Starting Jahia Server
echo ---------------------------------------------------------------------------

cd tomcat/bin
./startup.sh
tail -f ../logs/catalina.out