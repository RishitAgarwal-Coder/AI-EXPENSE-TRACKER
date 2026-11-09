@echo off
title SmartSpend - AI Expense Tracker
echo ========================================================
echo         SmartSpend - AI Expense Tracker (JavaFX)
echo ========================================================
echo.

REM Prefer the configured JDK so an older Java earlier in PATH is not used.
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
        set "JAVAC_EXE=%JAVA_HOME%\bin\javac.exe"
        set "PATH=%JAVA_HOME%\bin;%PATH%"
    )
)
if not defined JAVA_EXE set "JAVA_EXE=java"
if not defined JAVAC_EXE set "JAVAC_EXE=javac"

REM Check if Maven is available in PATH
where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [1/2] Maven detected! Launching via JavaFX Maven plugin...
    mvn clean javafx:run
    goto end
)

echo [!] Maven not found in PATH.
echo [2/2] Attempting to launch using javac and java...

set OPENJFX_DIR=%USERPROFILE%\.m2\repository\org\openjfx
set JFX_BASE=%OPENJFX_DIR%\javafx-base\21.0.6\javafx-base-21.0.6-win.jar
set JFX_CONTROLS=%OPENJFX_DIR%\javafx-controls\21.0.6\javafx-controls-21.0.6-win.jar
set JFX_GRAPHICS=%OPENJFX_DIR%\javafx-graphics\21.0.6\javafx-graphics-21.0.6-win.jar
set H2_JAR=%USERPROFILE%\.m2\repository\com\h2database\h2\2.2.224\h2-2.2.224.jar
set MYSQL_JAR=%USERPROFILE%\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar

set JFX_MODULE_PATH=%JFX_BASE%;%JFX_CONTROLS%;%JFX_GRAPHICS%
set CLASSPATH=target\classes;%H2_JAR%;%MYSQL_JAR%

if not exist target\classes (
    echo Compiling SmartSpend Java classes...
    if not exist target\classes mkdir target\classes
    "%JAVAC_EXE%" --module-path "%JFX_MODULE_PATH%" --add-modules javafx.controls -cp "%H2_JAR%;%MYSQL_JAR%" -d target\classes src\main\java\com\smartspend\*.java src\main\java\com\smartspend\*\*.java
    xcopy /E /I /Y src\main\resources\* target\classes\
)

"%JAVA_EXE%" --module-path "%JFX_MODULE_PATH%" --add-modules javafx.controls -cp "%CLASSPATH%" com.smartspend.Main

:end
pause
