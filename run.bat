@echo off
rem Starts the game on Windows. Builds the jar first if it is missing.
setlocal
cd /d "%~dp0"

set JAVA=java
if defined JAVA_HOME set "JAVA=%JAVA_HOME%\bin\java"

if not exist dist\Ludo_Alligator.jar call build.bat
"%JAVA%" -jar dist\Ludo_Alligator.jar
