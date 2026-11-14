# DEADBEAT

Videojuego de supervivencia 2D desarrollado en **Java 11**, **LibGDX** y **LWJGL3**. El protagonista es un músico punk que combate zombis con una guitarra eléctrica y unas baquetas, obtiene experiencia, selecciona mejoras y debe superar cinco oleadas. La victoria requiere terminar la quinta oleada y eliminar a los enemigos restantes.

## Contenido del proyecto

- `core/`: reglas, entidades, armas, movimiento, pantallas y pruebas JUnit.
- `lwjgl3/`: lanzador para equipos de escritorio.
- `assets/`: sprites, escenario, interfaz y audio utilizados durante la ejecución.
- `docs/`: informe ejecutivo, diagramas UML e instrucciones de instalación.
- `gradle/`, `gradlew` y `gradlew.bat`: Gradle Wrapper 7.6.4.

## Edición académica — Eclipse y Oracle JDK 11

La rúbrica de Programación Avanzada exige **Oracle JDK 11 u Oracle JDK 8**, además de **Eclipse o NetBeans 21 o inferior**. Deadbeat está preparado para Java 11; no se garantiza compatibilidad con Java 8. Para acreditar el requisito literalmente, la validación final debe ejecutarse con **Oracle JDK 11**.

1. Instalar Oracle JDK 11 y configurar `JAVA_HOME` para apuntar a la carpeta del JDK.
2. En una consola de Windows abierta dentro de `DeadBeat/`, ejecutar:

   ```powershell
   .\gradlew.bat --version
   .\gradlew.bat clean :core:test :lwjgl3:classes --no-daemon
   .\gradlew.bat :lwjgl3:run --no-daemon
   ```

3. Para importar el proyecto a Eclipse, ejecutar `Preparar-Eclipse.bat` y seguir [`docs/INSTALACION_ACADEMICA.md`](docs/INSTALACION_ACADEMICA.md). El proyecto utiliza `Deadbeat-Eclipse.launch` para establecer el directorio de recursos.

Los módulos `core` y `lwjgl3` utilizan `options.release = 11`. Gradle 7.6.4 admite JDK 11 y JDK 17.

## Edición portable para Windows

La distribución portable se genera con **JDK 17** y `jpackage`. Incluye el ejecutable, la JVM y las bibliotecas necesarias, por lo que el equipo destinatario no necesita instalar Java.

- **Desde Windows:** ejecutar `Crear-portable-Windows.bat` con JDK 17 completo configurado.
- **Desde GitHub:** abrir **Actions → Deadbeat - Windows portable → Run workflow**.
- **Resultado:** `DeadBeat/distribucion/Deadbeat-Windows-Portable.zip`.

El empaquetado no modifica las clases Java ni su compatibilidad con JDK 11. Leer [`docs/INSTALACION_PORTABLE.md`](docs/INSTALACION_PORTABLE.md) para instrucciones y verificaciones.

## Controles del juego

| Acción | Tecla / interacción |
|---|---|
| Moverse | WASD o flechas |
| Pausar o reanudar | ESC |
| Reiniciar partida | R |
| Elegir una mejora | 1, 2, 3 o clic |
| Pista anterior / siguiente | Q / E |
| Recibir daño de prueba | H (depuración) |
| Menú y pantallas finales | Teclado o ratón |

## Informe y UML

- **Informe ejecutivo:** [`docs/Informe_Ejecutivo_Deadbeat.pdf`](docs/Informe_Ejecutivo_Deadbeat.pdf) y [`docs/Informe_Ejecutivo_Deadbeat.docx`](docs/Informe_Ejecutivo_Deadbeat.docx).
- **Diagramas y maquetas:** `docs/figuras/` (PNG).
- **Fuentes editables de UML:** `docs/uml_fuentes/` (DOT y PlantUML).

El informe documenta los requisitos GM-1 a GM-10, incluyendo los patrones **Singleton** (`AudioSettings`), **Strategy** (`MovementStrategy`), **Template Method** (`Weapon`) y **Builder** (`WaveBuilder`). Las imágenes de GM-1 son **maquetas**, no capturas de ejecución.

## Control de calidad

Los flujos de integración continua validan la compilación y las pruebas del código con JDK 11 y JDK 17:

- [Académico JDK 11](https://github.com/Klonoh/Deadbeat/actions/workflows/deadbeat-academico-jdk11.yml)
- [Windows portable](https://github.com/Klonoh/Deadbeat/actions/workflows/deadbeat-portable-windows.yml)

Estas verificaciones automáticas no sustituyen la prueba manual con Oracle JDK 11 en Eclipse ni la apertura del ejecutable en otro equipo Windows.

## Derechos de distribución

El repositorio incorpora música de terceros. Antes de publicar o distribuir el videojuego fuera del contexto permitido, deben obtenerse los derechos de uso correspondientes o reemplazar las pistas por música autorizada. No se ha validado que exista autorización para redistribuir estas canciones.
