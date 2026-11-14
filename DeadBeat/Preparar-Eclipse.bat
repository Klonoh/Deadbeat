@echo off
setlocal
cd /d "%~dp0"
echo ==========================================
echo    DEADBEAT - EDICION ACADEMICA ECLIPSE
echo ==========================================
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\preparar-eclipse.ps1" %*
set "EXITCODE=%ERRORLEVEL%"
echo.
if not "%EXITCODE%"=="0" (
    echo La preparacion no termino correctamente. Revisa los mensajes anteriores.
) else (
    echo Proyecto Eclipse preparado. Lee docs\INSTALACION_ACADEMICA.md
)
echo.
pause
exit /b %EXITCODE%
