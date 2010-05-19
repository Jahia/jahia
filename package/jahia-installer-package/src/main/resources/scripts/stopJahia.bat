@echo off
if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Stopping Jahia Server
rem ---------------------------------------------------------------------------

cd %{INSTALL_PATH}\tomcat\bin
shutdown.bat