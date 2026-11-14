# Deadbeat — Instalación académica en Eclipse (Java 11)

Esta guía permite abrir, compilar y ejecutar la edición académica de Deadbeat en Eclipse. Se utiliza el mismo código fuente que genera la edición portable de Windows.

## Requisitos

- **Oracle JDK 11**, conforme a la exigencia literal de la pauta del curso. El proyecto no garantiza compatibilidad con Java 8.
- **Eclipse IDE for Java Developers**.
- Windows x64 y acceso a Internet la primera vez para descargar dependencias.
- Proyecto completo, incluidos `core/`, `lwjgl3/`, `assets/`, los archivos de Gradle y `Deadbeat-Eclipse.launch`.
- Gradle Wrapper **7.6.4** y LibGDX **1.14.2**.

Una distribución alternativa de Java 11 puede servir para desarrollar, pero no sustituye Oracle JDK 11 cuando se desea verificar literalmente la pauta.

## Preparación

1. Abrir PowerShell en `DeadBeat/`.
2. Ejecutar el asistente apuntando a la carpeta real de Oracle JDK 11:

   ```powershell
   .\scripts\preparar-eclipse.ps1 -JdkHome "C:\Program Files\Java\jdk-11" -ExigirOracle
   ```

   También se puede ejecutar `Preparar-Eclipse.bat` si `JAVA_HOME` apunta al JDK 11 adecuado.

3. El asistente comprueba Java 11 y Gradle 7.6.4, genera los archivos `.project`, `.classpath` y `.settings`, compila el lanzador de escritorio y confirma que exista la configuración de ejecución.

Los archivos de Eclipse se generan localmente y no se versionan. Se deben **incluir expresamente en el proyecto académico exportado**.

## Importar en Eclipse

1. Abrir Eclipse y elegir un workspace.
2. En **Window > Preferences > Java > Installed JREs**, agregar Oracle JDK 11 y establecerlo como predeterminado.
3. Ir a **File > Import > General > Existing Projects into Workspace**.
4. Seleccionar `DeadBeat/` y activar búsqueda de proyectos anidados si corresponde.
5. Deben aparecer `Deadbeat`, `Deadbeat-core` y `Deadbeat-lwjgl3`. No seleccionar **Copy projects into workspace**.
6. Comprobar que el compilador Java de cada módulo está configurado en la versión 11.

Como alternativa, se puede importar con **Gradle > Existing Gradle Project** (Buildship). No mezclar ambos mecanismos en un mismo workspace.

## Ejecutar

Abrir `Deadbeat-Eclipse.launch` desde el proyecto principal. Debe ejecutar `com.deadbeat.lwjgl3.Lwjgl3Launcher`, usando `${workspace_loc:/Deadbeat}/assets` como directorio de trabajo.

También se puede compilar y ejecutar desde PowerShell:

```powershell
java --version
javac -version
.\gradlew.bat --version
.\gradlew.bat clean :core:classes :lwjgl3:installDist --no-daemon
.\gradlew.bat :lwjgl3:run --no-daemon
```

## Archivos para la entrega académica

- Proyecto exportado desde Eclipse, incluyendo su configuración de proyectos y dependencias.
- Código Java, recursos y Gradle Wrapper.
- Informe ejecutivo y diagramas UML.
- Instrucciones de instalación y ejecución.

La comprobación automática de GitHub Actions usa Temurin 11. La validación con Oracle JDK 11 y una partida real debe realizarse por separado.
