# DEADBEAT - Registro de verificacion con JDK 11

**Estado inicial:** pendiente de ejecucion en Windows con Oracle JDK 11.
Los archivos de esta fase preparan la edicion Eclipse; no prueban por si solos
que el proyecto ha sido ejecutado con Oracle JDK 11.

## Evidencia requerida antes de entregar

| Comprobacion | Resultado esperado | Estado |
| --- | --- | --- |
| `java --version` | Version 11 | PENDIENTE |
| `javac -version` | Version 11 | PENDIENTE |
| Archivo `release` de JDK | `IMPLEMENTOR="Oracle Corporation"` | PENDIENTE |
| `gradlew --version` | Gradle 7.6.4, JVM 11 | PENDIENTE |
| `gradlew :core:test` | BUILD SUCCESSFUL | PENDIENTE |
| `gradlew :lwjgl3:classes` | BUILD SUCCESSFUL | PENDIENTE |
| Importacion en Eclipse | Sin errores de compilacion | PENDIENTE |
| `Deadbeat-Eclipse.launch` | Juego abre, renderiza y reproduce recursos | PENDIENTE |
| Partida manual | Juego funcional de inicio a fin | PENDIENTE |

## Comandos de captura

Desde `DeadBeat/` y con `JAVA_HOME` apuntando a Oracle JDK 11:

```powershell
java --version
javac -version
Get-Content "$env:JAVA_HOME\release" | Select-String 'IMPLEMENTOR|JAVA_VERSION'
.\gradlew.bat --version
.\gradlew.bat clean :core:test :lwjgl3:classes --no-daemon
.\gradlew.bat :lwjgl3:run --no-daemon
```

Registrar en el informe o anexo:

1. Fecha de la verificacion y version exacta de Oracle JDK 11.
2. Captura de Gradle 7.6.4 usando JVM 11.
3. Resultado de los tests y compilacion.
4. Captura de Eclipse con los tres proyectos importados.
5. Captura del juego lanzado desde Eclipse.
6. Resultado de una prueba manual con cinco oleadas y pantallas de fin.

No marcar las pruebas como completadas sin ejecutarlas realmente.

## Separacion respecto a la version portable

La version portable sigue usando JDK 17 para ejecutar `jpackage`.
Eso **no** modifica el bytecode Java 11 ni sustituye la validacion academica.
La validacion del ZIP portable en un PC sin Java es otra fase.
