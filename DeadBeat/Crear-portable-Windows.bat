@echo off
setlocal
cd /d "%~dp0"
echo ==========================================
echo     DEADBEAT - CREADOR DE VERSION PORTABLE
echo ==========================================
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\crear-portable-windows.ps1" %*
if errorlevel 1 goto fallo
echo.
echo Validando ZIP portable generado...
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\verificar-portable-windows.ps1"
if errorlevel 1 goto fallo
echo.
echo LISTO: distribucion\Deadbeat-Windows-Portable.zip
echo Para jugar en otra PC: extraer el ZIP completo y abrir Deadbeat.exe.
echo.
pause
exit /b 0
:fallo
echo.
echo ERROR: Revisa los mensajes de construccion y verificacion.
echo.
pause
exit /b 1
