@echo off
set MAVEN_GOAL=%1
set MODULE_NAME="-DsingleModuleName=%2"
if "%1" == "" goto setMavenGoal

:checkModuleName
if "%2" == "" goto setModuleName
goto callMaven

:setMavenGoal
set MAVEN_GOAL="install"
goto checkModuleName

:setModuleName
set MODULE_NAME=""

:callMaven
echo Maven goal  : %MAVEN_GOAL%
echo Module name : %MODULE_NAME%
mvn %MAVEN_GOAL% -DsingleModule=true %MODULE_NAME% %3 %4 %5
