# DEADBEAT - Instalacion academica en Eclipse (Java 11)

Esta guia permite abrir, compilar y ejecutar la edicion academica de Deadbeat en Eclipse.
Se usa **el mismo codigo fuente** que genera la version portable de Windows.

## Requisitos academicos

- **Oracle JDK 11** (exigencia literal de la pauta; Oracle JDK 8 tambien aparece permitido,
  pero Deadbeat esta configurado para Java 11 y no se garantiza funcionamiento con Java 8).
- **Eclipse IDE for Java Developers** (editor admitido en la pauta).
- Windows x64 y acceso a Internet la primera vez para resolver dependencias.
- Repositorio completo, incluida la carpeta `DeadBeat/assets/` y los modulos `core` y `lwjgl3`.
- El proyecto utiliza **Gradle Wrapper 7.6.4**, **LibGDX 1.14.2** y JUnit 4.13.2.

> Temurin 11 puede funcionar tecnicamente, pero **no reemplaza a Oracle JDK 11**
> cuando se quiere satisfacer la formulacion literal de la rubrica.

## A. Preparacion automatizada (recomendada)

1. Abrir PowerShell en `DeadBeat/` (la carpeta que contiene `gradlew.bat`).
2. Ejecutar el asistente con la ruta **real** de Oracle JDK 11:

   ```powershell
   .\scripts\preparar-eclipse.ps1 -JdkHome "C:\Program Files\Java\jdk-11" -ExigirOracle
   ```

   Si quieres probar primero con el Temurin JDK 11 instalado previamente:

   ```powershell
   .\Preparar-Eclipse.bat -JdkHome "C:\Java\jdk-11.0.33+10"
   ```

   En ese caso aparecera una advertencia de que la validacion literal Oracle esta pendiente.
   Alternativamente, haz doble clic en `Preparar-Eclipse.bat` si `JAVA_HOME`
   apunta al JDK 11 correcto.

3. El asistente hace lo siguiente:
   - Confirma que `java` sea version 11 y muestra el proveedor del JDK.
   - Verifica que el Gradle Wrapper sea 7.6.4.
   - Ejecuta `gradlew eclipse :lwjgl3:classes`.
   - Ejecuta las pruebas `:core:test` (omitelas solo para diagnostico con `-SinPruebas`).
   - Verifica `.project`, `.classpath`, `.settings` y el lanzador Eclipse.

Los archivos de Eclipse se generan localmente y estan excluidos de Git por `.gitignore`.
Para entregar el proyecto exportado se debe **incluirlos expresamente en el ZIP final**.

## B. Importar el proyecto en Eclipse

1. Abre Eclipse y selecciona o crea un workspace vacio.
2. Abre **Window > Preferences > Java > Installed JREs**.
3. Pulsa **Add > Standard VM**, selecciona la raiz de **Oracle JDK 11** y establecelo
   como predeterminado. No selecciones un JRE sin compilador.
4. Abre **File > Import > General > Existing Projects into Workspace**.
5. En **Select root directory** selecciona la carpeta `DeadBeat/`, no la carpeta
   exterior del repositorio.
6. Comprueba que aparecen los tres proyectos `Deadbeat`, `Deadbeat-core`
   y `Deadbeat-lwjgl3`. Si es necesario, activa **Search for nested projects**.
7. **No marques Copy projects into workspace**: manten los recursos y rutas originales.
8. Finaliza y espera a que Eclipse resuelva las dependencias.
9. Si Eclipse utiliza un JRE diferente, abre las propiedades de cada proyecto
   (**Java Build Path**) y configura JDK 11; confirma que el nivel de compilador es 11.

Si prefieres usar el complemento Buildship, tambien puedes utilizar
**File > Import > Gradle > Existing Gradle Project**, seleccionando `DeadBeat/`
y configurando el Gradle JVM de la importacion con JDK 11. No combines ambas
formas de importacion en el mismo workspace para evitar proyectos duplicados.

## C. Ejecutar el juego desde Eclipse

1. En el proyecto `Deadbeat`, localiza `Deadbeat-Eclipse.launch`.
2. Clic derecho sobre el archivo y selecciona **Run As** (o abre
   **Run > Run Configurations...** y selecciona la configuracion guardada).
3. Debe ejecutar `com.deadbeat.lwjgl3.Lwjgl3Launcher` del proyecto
   `Deadbeat-lwjgl3`.
4. El **Working directory** es `${workspace_loc:/Deadbeat}/assets`.
   Esta carpeta es esencial para que LibGDX encuentre sprites, mapas y musica.

Si el lanzador no aparece en Eclipse, crea una configuracion **Java Application**
con esos tres datos: proyecto, clase principal y directorio de trabajo.

## D. Ejecucion alternativa sin Eclipse

En PowerShell, con Oracle JDK 11 seleccionado:

```powershell
java --version
javac -version
.\gradlew.bat --version
.\gradlew.bat clean :core:test :lwjgl3:classes --no-daemon
.\gradlew.bat :lwjgl3:run --no-daemon
```

El `:lwjgl3:run` ya define `workingDir = rootProject.file('assets')`.

## E. Materiales de la entrega academica

La rubrica solicita expresamente:

- Instrucciones de instalacion y ejecucion (esta guia).
- Codigo fuente y **export del proyecto Eclipse** con metadata `.project`, `.classpath`
  y `.settings` generada desde el repositorio completo.
- Recursos necesarios: `assets/`, bibliotecas resueltas por el Wrapper y, si el
  formato de entrega lo exige, los JAR pertinentes.
- Reporte ejecutivo con GM-1 a GM-10, diagramas UML y evidencia en codigo.
- Copia del Gradle Wrapper y los modulos `core` / `lwjgl3`.

El paquete academico se prepara de forma independiente de la distribucion portable.
Debe incluir el proyecto exportado desde Eclipse, codigo fuente, recursos,
dependencias y el informe ejecutivo.

## F. Solucion de problemas

- `Gradle requires JVM 17 or later`: estas usando un wrapper distinto a 7.6.4
  o una configuracion desactualizada. Revisa `gradle/wrapper/gradle-wrapper.properties`.
- `Unsupported class file major version`: verifica la version real del JDK
  en Eclipse, en Gradle y en `JAVA_HOME`; limpia y recompila.
- `Couldn't load file sprites/...` o musica faltante: revisa el **Working directory**
  de la configuracion Eclipse: debe terminar en `DeadBeat/assets`.
- No aparecen los proyectos: ejecuta nuevamente `Preparar-Eclipse.bat`, verifica
  que existan `.project` y `.classpath`, y repite la importacion.
- `jpackage` no se encuentra: este requisito **no aplica** a la edicion academica.
  El empaquetado portable se construye por separado usando JDK 17.
