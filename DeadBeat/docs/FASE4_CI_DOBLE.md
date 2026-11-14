# DEADBEAT - Fase 4: validacion automatica de dos distribuciones

## Objetivo

Mantener **un unico codigo fuente** con dos verificaciones independientes:

1. **Academica JDK 11**: compilar `core` y `lwjgl3` con JDK 11, ejecutar
   los tests JUnit y verificar que ambas clases principales son bytecode 55
   (compatible con Java 11).
2. **Portable Windows JDK 17**: construir el EXE con `jpackage`, ejecutar
   los tests JUnit y comprobar que el ZIP contiene la JVM reducida, bibliotecas
   y recursos necesarios.

El JDK 17 **no cambia** `options.release = 11`. No hay duplicacion del juego.

## Estado de partida y correccion incluida

La ejecucion de la Fase 3, identificada como GitHub Actions `43537662261`,
compilo el juego, genero el EXE y luego fallo al buscar
`runtime/bin/java.exe` en el paquete producido por `jpackage`.

`jpackage` usa por defecto `jlink --strip-native-commands`, de forma que puede
omitir `java.exe`. El verificador se corrigio: ahora comprueba
`runtime/release` (version 17), `runtime/bin/java.dll`,
`runtime/bin/server/jvm.dll`, las clases principales, las dependencias,
los recursos y los archivos correspondientes dentro del ZIP.

## Archivos de esta fase

- `.github/workflows/deadbeat-academico-jdk11.yml` - nuevo flujo JDK 11.
- `.github/workflows/deadbeat-portable-windows.yml` - flujo JDK 17 con tests.
- `DeadBeat/scripts/verificar-portable-windows.ps1` - reemplazo corregido.
- `DeadBeat/docs/FASE4_CI_DOBLE.md` - esta guia.

No se modifican `Player.java`, `GameWorld.java`, `core/build.gradle`,
`lwjgl3/build.gradle`, Eclipse ni el script que construye el EXE.

## Ejecutar en GitHub

1. Colocar los archivos anteriores en sus rutas y realizar un commit.
2. Ir a GitHub > Actions.
3. Comprobar que aparezcan los flujos:
   - `Deadbeat - Academico JDK 11`
   - `Deadbeat - Windows portable`
4. Ambos se ejecutan automaticamente en `push` a `main` que cambie el proyecto,
   y en pull requests con archivos relevantes. Tambien admiten `Run workflow`.
5. Los checks deben estar en verde sobre **el mismo SHA de commit**.
6. En el flujo portable descargar el artefacto `Deadbeat-Windows-Portable`.
   El ZIP solo se publica en `main` o en ejecuciones manuales, no en PR.

## Probar Oracle JDK 11 localmente (exigencia literal de la rubrica)

El CI usa **Temurin JDK 11** para comprobar la compatibilidad tecnica sin
necesitar credenciales ni aceptar una licencia de Oracle en GitHub.
La rubrica exige **Oracle JDK 11 o 8**, por lo que debe comprobarse tambien
manualmente con Oracle JDK 11 desde Windows/Eclipse:

```powershell
# Desde DeadBeat/; sustituir por la ruta real de Oracle JDK 11.
.\scripts\preparar-eclipse.ps1 -JdkHome 'C:\Program Files\Java\jdk-11' -ExigirOracle
.\gradlew.bat :core:test :lwjgl3:classes
.\gradlew.bat :lwjgl3:run
```

El script configura `JAVA_HOME` solo en su proceso; para los comandos
posteriores, seleccione el JDK 11 en la terminal o configuracion de Eclipse.
No declare cumplimiento literal de Oracle hasta completar esa validacion.

## Comprobaciones manuales que NO cubre el CI

- Abrir la ventana `Deadbeat.exe` en un Windows x64 sin Java instalado.
- Ver el menu, iniciar una partida, verificar sprites, mapas, sonido, mejoras
  y avance por las cinco oleadas.
- Comprobar pantallas de victoria y derrota y cierre del programa.
- Actualizar el reporte y los UML de GM-1 a GM-10 antes de la entrega academica.

## Si falla algun workflow

- **JDK11:** revisar el test JUnit que fallo, errores de compilacion, la JVM
  mostrada por `gradlew --version` y el bytecode que muestra `javap`.
- **Portable:** revisar el paso `-Probar`, el error preciso del verificador
  y que el ZIP incluya la carpeta `Deadbeat` completa.
- No publicar ni entregar el paquete como verificado hasta ver ambas
  comprobaciones exitosas y probar el ejecutable manualmente.

## Distribucion y licencias

La musica de terceros incorporada a `assets/music` no debe distribuirse
publicamente sin licencias o permisos. Sustituirla por recursos propios o
con licencia adecuada antes de publicar una Release abierta.
