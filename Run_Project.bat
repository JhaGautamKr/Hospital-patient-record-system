@echo off
setlocal
cd /d "%~dp0"

echo ====================================================================
echo      HOSPITAL PATIENT RECORD SYSTEM - ONE-CLICK LAUNCHER
echo ====================================================================
echo.

:: Check Java
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java is not installed or not found in system PATH.
    echo Please install Java JDK or JRE to run this project.
    pause
    exit /b 1
)

:: Compile if not compiled yet
if not exist "bin\com\hospital\Main.class" (
    echo [1/3] First-time setup detected. Compiling Java backend...
    call compile.bat
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Compilation failed. Please inspect errors above.
        pause
        exit /b 1
    )
    echo.
)

echo [2/3] Starting Hospital Backend Server with MySQL Connectivity...
start "Hospital Record System Server" java -cp "bin;lib/mysql-connector-j-8.3.0.jar" com.hospital.Main 8080

echo [3/3] Waiting for server to initialize...
timeout /t 2 /nobreak >nul

echo.
echo Opening Hospital System in your default web browser...
start "" "http://localhost:8080"

echo.
echo ====================================================================
echo   PROJECT RUNNING SUCCESSFULLY!
echo   Web Address : http://localhost:8080
echo   Database    : MySQL (hospital_db)
echo.
echo   To STOP the system, close the "Hospital Record System Server" window.
echo ====================================================================
echo.
