# DEADBEAT - Prototipo 0.1

Juego 2D de supervivencia contra zombies con estetica punk, creado para el proyecto de Programacion Avanzada.
Este primer prototipo solo muestra una arena y una marca neon animada para verificar que el ciclo de renderizado funciona. Aun no hay jugador, zombies ni armas.

## Requisitos

- JDK 11 para compilar y ejecutar este proyecto (el codigo fuente usa Java 11).
- Internet en el primer inicio, para que Gradle descargue dependencias y su distribucion.
- Eclipse con Buildship o NetBeans con soporte para Gradle (o bien terminal).

El wrapper se ha configurado con Gradle 7.6.4, compatible con ejecucion en JDK 11.
Dependencia: libGDX 1.14.2 y backend LWJGL3.

## Ejecutar

Desde la carpeta raiz del proyecto:

**Windows, PowerShell:**

```powershell
.\gradlew.bat :lwjgl3:run
```

**Linux o macOS:**

```bash
./gradlew :lwjgl3:run
```

Para comprobar la compilacion sin abrir una ventana:

```powershell
.\gradlew.bat :core:compileJava :lwjgl3:compileJava
```

Si estas en NetBeans o Eclipse, importa la carpeta raiz como proyecto Gradle. Ejecuta la tarea `lwjgl3 -> application -> run`.
Usa JDK 11 como Java de Gradle. Si utilizas un JDK mas moderno para compilar, consulta la matriz de compatibilidad de Gradle.

## Que deberias ver

- Una ventana 960x540 llamada `DEADBEAT - Prototipo 0.1`.
- Arena oscura con bordes fucsia y cuadrícula.
- Un pequeño indicador verde que late en el centro (demuestra `update(delta)`).
- Contador de segundos que avanza.
- Tecla `ESC` para salir.

Si ves lo anterior, los tres componentes principales estan comunicandose correctamente.

## Estructura del proyecto

```
Deadbeat_Inicio/
├── core/
│   └── src/main/java/com/deadbeat/
│       ├── DeadbeatGame.java
│       ├── screens/GameScreen.java
│       └── world/GameWorld.java
├── lwjgl3/
│   └── src/main/java/com/deadbeat/lwjgl3/Lwjgl3Launcher.java
├── assets/
├── gradle/wrapper/
├── gradlew
├── gradlew.bat
├── settings.gradle
├── build.gradle
└── README.md
```

## Responsables sugeridos

- **Programador 1:** `GameScreen`, luego `Player` y control del teclado.
- **Programador 2:** `GameWorld` y luego `Zombie` y estrategias de movimiento.
- **Ambos:** revisar contratos e integrar funcionalidades en `DeadbeatGame`.

## Siguiente fase

Implementar `Player`, `Zombie`, `Damageable`, `MovementStrategy` y `ChaseMovement`.
En esta fase se acuerda si se dibujara cada entidad con sprites o con la textura de prueba.

## Nota

El codigo fue escrito para Deadbeat y no copia clases ni metodos de los juegos de referencia. La carpeta `gradle/wrapper` contiene herramientas de compilacion de Gradle (no logica de videojuego).
