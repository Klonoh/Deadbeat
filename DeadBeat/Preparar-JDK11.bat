@echo off
setlocal
cd /d "%~dp0"
echo =========================================
echo   DEADBEAT - COMPATIBILIDAD JDK 11
echo =========================================
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\preparar-jdk11.ps1" %*
set "EXITCODE=%ERRORLEVEL%"
echo.
if not "%EXITCODE%"=="0" (
    echo No se completo la validacion. Revisa el mensaje de error.
) else (
    echo Wrapper actualizado y verificado.
)
echo.
pause
exit /b %EXITCODE%
