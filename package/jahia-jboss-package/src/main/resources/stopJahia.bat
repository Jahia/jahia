@echo off
if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Stop Script for Jahia Server
rem ---------------------------------------------------------------------------

set NOPAUSE=true

bin\shutdown.bat -S