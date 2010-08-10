@echo off
if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Stopping Jahia Server
rem ---------------------------------------------------------------------------

cd tomcat\bin
shutdown.bat