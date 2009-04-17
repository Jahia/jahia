@echo off
echo Checking environment...
set BINDIR=%CD%
if not EXIST %CD%\..\jre\jre\bin\java.exe goto nojre
echo JRE found, setting JAVA_HOME to point to JRE...
set JAVA_HOME=%CD%\..\jre\jre
:nojre
if "a%JAVA_HOME%"==a goto javahomeerror
if not "a%CATALINA_HOME%"==a goto catalinaseterror

:aftercatalina
if not "a%TOMCAT_HOME%"==a goto tomcatseterror

:aftertomcat
if not EXIST %JAVA_HOME%\lib\tools.jar goto testforjikes
rem if not EXIST %JAVA_HOME%\bin\javac.exe goto testforjikes
goto startservers

:testforjikes
if not EXIST %BINDIR%\..\jikes\bin\jikes.exe goto invalidjdk

:startservers
set PATH=%JAVA_HOME%\bin;%PATH%
echo Ok. Starting servers...
if EXIST %BINDIR%\..\jikes\bin\jikes.exe goto havejikes
 call bin\catalina.bat start
goto skipjikes
:havejikes
set PATH=%BINDIR%\..\jikes\bin;%PATH%
call bin\catalina-jikes.bat start
:skipjikes
cd webapps/config/WEB-INF/classes
java -classpath "%CLASSPATH%;.\;.\WEB-INF\lib\log4j-1.2.15.jar" org.jahia.init.TomcatWaitInstallation
start http://localhost:8080/config/html/startup/loadingjahiaServer.jsp
echo Starting browser...
echo Done. Please wait while systems initialize...
goto end

:javahomeerror
echo JAVA_HOME variable must be set!
pause
goto end

:catalinaseterror
echo CATALINA_HOME variable must NOT be set!
set CATALINA_HOME=
goto aftercatalina

:tomcatseterror
echo TOMCAT_HOME variable must NOT be set!
set TOMCAT_HOME=
goto aftertomcat

:invalidjdk
echo Either JAVA_HOME does not point to correct location or the JDK installation is invalid, or JIKES couldn't be found.
pause
goto end

:end
