@echo off
if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Start Script for Jahia Server
rem ---------------------------------------------------------------------------

set NOPAUSE=true

start "Jahia Server" bin\run.bat -b 0.0.0.0

cd server\default\deploy\config.war\WEB-INF\classes
java -classpath . org.jahia.init.TomcatWaitInstallation http://localhost:8080/config/html/startup/startjahia.html
start http://localhost:8080/config/html/startup/loadingjahiaServer.jsp
echo Starting browser...
echo Done. Please wait while systems initialize...
