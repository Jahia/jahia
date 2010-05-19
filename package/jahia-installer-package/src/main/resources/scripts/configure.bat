@echo off
if "%OS%" == "Windows_NT" setlocal
echo ---------------------------------------------------------------------------
echo Configuring Jahia
echo ---------------------------------------------------------------------------

java -jar %{INSTALL_PATH}\build\configurators.jar %1
