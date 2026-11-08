@echo off
rem Builds dist\Ludo_Alligator.jar on Windows. Needs JDK 17+ on the PATH or JAVA_HOME set.
setlocal
cd /d "%~dp0"

set BIN=
if defined JAVA_HOME set "BIN=%JAVA_HOME%\bin\"

if exist build rmdir /s /q build
mkdir build\classes
if not exist dist mkdir dist

echo Compiling...
dir /s /b src\main\java\*.java > build\sources.txt
"%BIN%javac" --release 17 -encoding UTF-8 -d build\classes @build\sources.txt || exit /b 1
if exist src\main\resources xcopy /e /i /y /q src\main\resources build\classes >nul

echo Packaging dist\Ludo_Alligator.jar...
"%BIN%jar" --create --file dist\Ludo_Alligator.jar --main-class Controller.Main -C build\classes . || exit /b 1

echo Done. Start the game with run.bat or: java -jar dist\Ludo_Alligator.jar
