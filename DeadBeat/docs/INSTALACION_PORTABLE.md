# Deadbeat — Edición portable para Windows

Esta guía explica cómo generar y ejecutar Deadbeat en Windows como una carpeta portable que incluye la JVM.

## Contenido del paquete

Al construir el juego se obtiene `DeadBeat/distribucion/Deadbeat-Windows-Portable.zip`, que incluye:

- `Deadbeat/Deadbeat.exe`: lanzador de Windows.
- `Deadbeat/runtime/`: entorno Java reducido.
- `Deadbeat/app/`: JAR del juego, bibliotecas y recursos.

En otro equipo se debe **extraer la carpeta completa** y ejecutar `Deadbeat/Deadbeat.exe`. No es necesario instalar Java ni Gradle, y no se deben separar las carpetas `runtime/` y `app/` del ejecutable.

## Generar desde Windows

1. Descargar el repositorio completo.
2. Instalar un **JDK 17** que incluya `java.exe`, `jar.exe` y `jpackage.exe`.
3. En `DeadBeat/`, ejecutar `Crear-portable-Windows.bat` o, desde PowerShell, el siguiente comando con la ruta real del JDK:

   ```powershell
   .\scripts\crear-portable-windows.ps1 -JdkHome 'C:\ruta\al\jdk-17'
   ```

4. Comprobar la distribución generada:

   ```powershell
   .\scripts\verificar-portable-windows.ps1
   ```

El empaquetador ejecuta `:lwjgl3:installDist`, reúne las dependencias y recursos en el directorio de la aplicación y utiliza `jpackage --type app-image` para generar el ejecutable con Java incluido.

## Generar desde GitHub Actions

1. Abrir **Actions > Deadbeat - Windows portable**.
2. Seleccionar **Run workflow**.
3. Confirmar que las etapas de compilación, empaquetado y verificación finalizan correctamente.
4. Descargar el artefacto **Deadbeat-Windows-Portable**, extraer el ZIP interior si GitHub lo entrega anidado y abrir `Deadbeat/Deadbeat.exe`.

## Compatibilidad y limitaciones

- La construcción portable utiliza **JDK 17**; el código del proyecto conserva el objetivo de Java 11.
- `jpackage` debe ejecutarse en Windows para obtener un ejecutable Windows.
- El lanzador no está firmado digitalmente; Windows puede mostrar una advertencia de seguridad.
- La verificación automatizada comprueba la estructura, recursos y JVM incluida, pero no reemplaza iniciar una partida real y comprobar los controles, audio y pantallas finales.
- Las pistas musicales pertenecen a terceros: se deben revisar los derechos de uso antes de redistribuir públicamente el juego.
