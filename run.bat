@echo off
setlocal
echo ===============================================================
echo     Launching Staff Training Management System (STMS)
echo ===============================================================

REM Locate Java
if exist "C:\Program Files\Apache NetBeans\jdk\bin\java.exe" (
    set "JAVA_EXE=C:\Program Files\Apache NetBeans\jdk\bin\java.exe"
) else if exist "C:\Program Files\BlueJ\jdk\bin\java.exe" (
    set "JAVA_EXE=C:\Program Files\BlueJ\jdk\bin\java.exe"
) else (
    set "JAVA_EXE=java"
)

REM Check if target JAR exists
if exist "target\staff-training-management-system-1.0.0.jar" (
    echo Starting from packaged JAR...
    "%JAVA_EXE%" -jar "target\staff-training-management-system-1.0.0.jar"
) else (
    echo Running from compiled classes...
    "%JAVA_EXE%" -cp "target\classes" com.stms.Main
)

endlocal
pause
