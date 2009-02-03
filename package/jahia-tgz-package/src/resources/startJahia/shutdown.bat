@echo off
set ESI_MODE=OFF
echo Checking environment...
if not EXIST %CD%\..\jre\jre\bin\java.exe goto nojre
echo JRE found, setting JAVA_HOME to it.
set JAVA_HOME=%CD%\..\jre\jre
:nojre
if "a%JAVA_HOME%"==a goto javahomeerror
if not "a%CATALINA_HOME%"==a goto catalinaseterror
:aftercatalina
if not "a%TOMCAT_HOME%"==a goto tomcatseterror
:aftertomcat
:shutting
set PATH=%JAVA_HOME%\bin;%PATH%
echo Ok. Shutting down servers...
rem cd ..\hsqldb\demo
rem call shutdown.bat
rem cd ..\..
call bin\shutdown.bat
del webapps\jahia\.lock
cd ..\bin
echo Done.

goto end
:javahomeerror
echo JAVA_HOME variable must be set !
pause
goto end
:catalinaseterror
echo CATALINA_HOME variable must NOT be set !
set CATALINA_HOME=
goto aftercatalina
:tomcatseterror
echo TOMCAT_HOME variable must NOT be set !
set TOMCAT_HOME=
goto aftertomcat
pause
goto end
:end
