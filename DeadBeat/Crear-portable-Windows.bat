@echo off
setlocal
cd /d "%~dp0"
echo ==========================================
echo     DEADBEAT - CREADOR DE VERSION PORTABLE
echo ==========================================
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\crear-portable-windows.ps1" %*
set "EXITCODE=%ERRORLEVEL%"
echo.
if not "%EXITCODE%"=="0" (
    echo La construccion fallo. Revisa el error mostrado arriba.
) else (
    echo ZIP creado en distribucion\Deadbeat-Windows-Portable.zip
)
echo.
pause
exit /b %EXITCODE%
