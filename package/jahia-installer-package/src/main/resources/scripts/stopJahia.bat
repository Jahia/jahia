@echo off
if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Stopping Jahia Server
rem ---------------------------------------------------------------------------

cd %~dp0\tomcat\bin
call shutdown.bat
exit /b 0