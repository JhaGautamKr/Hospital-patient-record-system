@echo off
setlocal
cd /d "%~dp0"

echo ====================================================================
echo   Compiling Hospital Patient Record System (Java Backend)
echo ====================================================================

if not exist "bin" mkdir "bin"

echo Compiling Java source files...
javac -encoding UTF-8 -cp "lib\mysql-connector-j-8.3.0.jar" -d bin src\com\hospital\*.java src\com\hospital\model\*.java src\com\hospital\dao\*.java src\com\hospital\util\*.java src\com\hospital\server\*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Compilation completed successfully!
    echo Classes compiled to .\bin
) else (
    echo.
    echo [ERROR] Compilation failed! Please check error messages above.
)
