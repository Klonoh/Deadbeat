# DEADBEAT - Versión portable Windows (EXE con Java integrado)

Esta automatización fue adaptada al repositorio `Klonoh/Deadbeat` y a su
estructura real: `DeadBeat/core`, `DeadBeat/lwjgl3`, `DeadBeat/assets`.

## Resultado

El proceso genera en `DeadBeat/distribucion/`:

- `Deadbeat/Deadbeat.exe` - ejecutable de Windows.
- `Deadbeat/runtime/` - entorno Java incluido.
- `Deadbeat/app/` - JAR del juego, bibliotecas y `deadbeat-assets.jar`.
- `Deadbeat-Windows-Portable.zip` - archivo que se puede pasar a otra PC.

Quien recibe el ZIP debe **extraer toda la carpeta** y ejecutar
`Deadbeat/Deadbeat.exe`. No tiene que instalar Java ni Gradle, y no debe
separar el EXE de las carpetas `app` y `runtime`.

## Generar en tu PC con Windows

1. Descarga o actualiza el repositorio completo.
2. Instala **Temurin JDK 17**, no solo JRE. Para construir se necesitan
   `java.exe`, `jar.exe` y `jpackage.exe`.
3. En `DeadBeat/`, ejecuta `Crear-portable-Windows.bat` con doble clic.
4. Si Java 17 no está configurado en `JAVA_HOME`, abre PowerShell en `DeadBeat/`
   y pasa la ruta de instalación explícitamente:

   ```powershell
   .\scripts\crear-portable-windows.ps1 -JdkHome "C:\Program Files\Eclipse Adoptium\jdk-17.0.xx-hotspot"
   ```

   Sustituye esa ruta de ejemplo por la carpeta real de tu JDK 17, la que
   contiene `bin/`, `lib/`, `jmods/`, etc. **No** incluyas `\bin`.
5. Al finalizar, comparte `distribucion/Deadbeat-Windows-Portable.zip`.

También puedes comprobar los tests antes de generar el ZIP:

```powershell
.\scripts\crear-portable-windows.ps1 -Probar
```

## Generar mediante GitHub Actions (sin instalar nada en tu PC)

Coloca también el archivo `.github/workflows/deadbeat-portable-windows.yml`
en la **raíz del repositorio** (junto a la carpeta `DeadBeat/`).

1. Sube los nuevos archivos a tu repositorio GitHub.
2. En GitHub ve a **Actions** > **Deadbeat - Windows portable**.
3. Selecciona **Run workflow**.
4. Una vez que termine correctamente, descarga el artefacto
   **Deadbeat-Windows-Portable** desde la ejecución.
5. Extrae el ZIP final y ejecuta `Deadbeat.exe` dentro de la carpeta Deadbeat.

## Cómo funciona

- `:lwjgl3:installDist` del plugin `application` reúne el código de `core`,
  el lanzador `com.deadbeat.lwjgl3.Lwjgl3Launcher` y todas las dependencias
  Java, LibGDX y LWJGL3 de escritorio.
- `jar` crea `deadbeat-assets.jar` con el contenido íntegro de `assets/`,
  incluyendo sprites, mapa, canciones y `tracklist.properties`.
- `Gdx.files.internal` de LibGDX en desktop usa los archivos del directorio
  de trabajo y, si no están, consulta el classpath. Por eso los recursos
  dentro de `deadbeat-assets.jar` son accesibles aunque el juego se abra
  desde el Explorador de Windows.
- `jpackage --type app-image` genera el lanzador nativo de Windows junto a
  su entorno Java. No crea un instalador MSI ni requiere permisos de admin
  para ejecutar la carpeta portable.
- El script comprime *toda* la carpeta de la aplicación en un ZIP.

## Requisitos y limitaciones

- Para **crear** el paquete: PC Windows x64, JDK 17 y conexión para descargar
  dependencias en la primera compilación. El Gradle Wrapper del repositorio
  principal es 7.6.4 y el código fuente se compila apuntando a Java 11.
- Para **jugar**: Windows x64 compatible y capacidad de ejecutar aplicaciones
  de escritorio con OpenGL. Ni JDK ni Gradle instalados.
- Debido a restricciones del sistema operativo, el EXE Windows **no puede
  compilarse con jpackage desde Linux o macOS**; usa una PC Windows o el flujo
  de GitHub Actions incluido.
- Esta automatización no modifica código Java ni reemplaza el `Deadbeat.exe`
  antiguo de 3 KB que existe en la raíz de `DeadBeat`. El EXE nuevo está
  únicamente en la carpeta `distribucion/Deadbeat/`.
- **Derechos de la banda sonora:** el repositorio incluye canciones de
  terceros. Comprueba licencias o permisos antes de publicar o distribuir
  copias con esa música.

## Diagnóstico

**`Gradle requires JVM 17 or later`**: configura Temurin JDK 17 al crear
el paquete. El script fuerza el `JAVA_HOME` correcto durante la compilación.

**`No se encontro JDK`**: pasa `-JdkHome` apuntando al directorio del JDK.

**`java` no se reconoce**: no hace falta cambiar el `PATH` global si usas
`-JdkHome`; el script lo configura solo para su ejecución.

**El EXE no abre**: asegúrate de haber extraído `Deadbeat.exe`, `app/` y
`runtime/` juntos. Si Windows SmartScreen muestra una advertencia, puede
ser porque la aplicación no está firmada digitalmente. Para distribución
pública conviene firmarla con un certificado de desarrollador.

**La música no suena**: confirma que `assets/music/*.mp3` esté presente
al construir. El script los incorpora en `deadbeat-assets.jar`.
