@echo off
if "%OS%" == "Windows_NT" setlocal
echo ---------------------------------------------------------------------------
echo Starting Jahia Server
echo ---------------------------------------------------------------------------

call bin\startup.bat

cd webapps\config\WEB-INF\classes
java -classpath . org.jahia.init.TomcatWaitInstallation http://localhost:8080/config/html/startup/startjahia.html
start http://localhost:8080/config/html/startup/loadingjahiaServer.jsp
echo Starting browser...
echo Done. Please wait while systems initialize...
