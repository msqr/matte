@echo off

rem Guess CATALINA_HOME if not defined
set CURRENT_DIR=%cd%
if not "%CATALINA_HOME%" == "" goto gotHome
set CATALINA_HOME=%CURRENT_DIR%\apache-tomcat
if exist "%CATALINA_HOME%\bin\catalina.bat" goto okHome
cd ..
set CATALINA_HOME=%cd%
cd %CURRENT_DIR%
:gotHome
if exist "%CATALINA_HOME%\bin\catalina.bat" goto okHome
echo The CATALINA_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:okHome

set CATALINA_OPTS="-Dderby.system.home=%CATALINA_HOME%\..\matte\db"

echo Using CATALINA_OPTS:   %CATALINA_OPTS%

call "%CATALINA_HOME%\bin\catalina.bat" start

:end
