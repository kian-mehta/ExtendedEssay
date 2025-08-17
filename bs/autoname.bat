@echo off
REM AutoNamer - Rename .txt files based on their content summary
REM Usage: autoname [directory_path]

setlocal

REM Get the directory where this batch file is located
set "SCRIPT_DIR=%~dp0"

REM Set the path to the Python script
set "PYTHON_SCRIPT=%SCRIPT_DIR%autonamer.py"

REM Check if Python script exists
if not exist "%PYTHON_SCRIPT%" (
    echo Error: autonamer.py not found in %SCRIPT_DIR%
    echo Please ensure autonamer.py is in the same directory as this batch file.
    pause
    exit /b 1
)

REM Check if Python is available
python --version >nul 2>&1
if errorlevel 1 (
    echo Error: Python is not installed or not in PATH
    echo Please install Python and add it to your PATH environment variable.
    pause
    exit /b 1
)

REM Run the Python script with the provided argument (or current directory if none)
if "%~1"=="" (
    echo Running AutoNamer in current directory...
    python "%PYTHON_SCRIPT%" "%cd%"
) else (
    echo Running AutoNamer in directory: %~1
    python "%PYTHON_SCRIPT%" "%~1"
)

if errorlevel 1 (
    echo AutoNamer encountered an error.
    pause
)

endlocal
