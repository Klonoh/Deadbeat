@echo off
setlocal
cd /d "%~dp0"
echo ===========================================
echo     DEADBEAT - REPARAR GAMEWORLD
echo ===========================================
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\restaurar-sistema-mejoras.ps1" %*
set "EXITCODE=%ERRORLEVEL%"
echo.
if not "%EXITCODE%"=="0" (
    echo La correccion no se completo. Consulta el error mostrado arriba.
) else (
    echo Correccion aplicada. Verifica la compilacion y sube el cambio a GitHub.
)
echo.
pause
exit /b %EXITCODE%
