@echo off
if "%OS%" == "Windows_NT" setlocal
echo ---------------------------------------------------------------------------
echo Starting Jahia Server
echo ---------------------------------------------------------------------------

cd %~dp0\tomcat\bin
startup.bat
