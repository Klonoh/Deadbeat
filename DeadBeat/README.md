## Actualización 0.5.2: texturas ancladas al mundo
Se eliminó la modificación de UV en función de la cámara. El suelo se dibuja
sobre baldosas fijas de 64 × 64 unidades de mundo. Ver `docs/WORLD_0_5_2_TEXTURE_FIX.md`.

# Deadbeat 0.5.1 — Reparación de baldosas y estabilidad visual

Esta versión parte de 0.5, conserva su ciudad, sprites y colisiones, y corrige defectos de los pavimentos y la cámara.

**Cambios:** los materiales de suelo son PNGs repetibles independientes en `assets/world2/surfaces`; las avenidas y calles se dibujan como geometrías continuas, y las líneas viales se trazan por eje de carretera, sin depender de tiles. La cámara y el protagonista se alinean a píxeles enteros (solo la representación gráfica: la física conserva decimales). Se ajustó la cadencia visual de pasos.

Para Windows: `gradlew.bat :lwjgl3:run`. Pruebas: `gradlew.bat :core:test`.

No se modificaron las colisiones ni las posiciones de edificios, de modo que la ciudad y las calles conservan su navegabilidad.

# DEADBEAT — Ciudad Punk 0.5

Prototipo LibGDX en Java 11: personaje punk animado en un mundo urbano postapocalíptico generado de forma determinista. Esta entrega actualiza el motor de escenarios del prototipo 0.4.

## Ejecutar (Windows)

1. Extraer el ZIP y entrar en la carpeta principal del proyecto.
2. Abrir PowerShell y ejecutar:

```powershell
.\gradlew.bat :lwjgl3:run
```

En Linux/macOS: `./gradlew :lwjgl3:run`. La primera ejecución requiere conexión a internet para las dependencias Gradle/LibGDX. JDK recomendado: Java 11. El wrapper conserva Gradle 7.6.4.

## Controles

- `W A S D` o flechas: caminar y explorar (cámara centrada).
- `H`: recibir 20 puntos de daño para pruebas.
- `R`: regresar al punto inicial y restaurar la vida.
- `ESC`: salir.

## Esta versión

- Calles principales continuas en dos ejes, con anchos variables y pasos peatonales.
- Manzanas rectangulares irregulares, edificios de diferentes tamaños, pasajes y lotes abiertos.
- 6 sectores reproducibles: tiendas, viviendas, industria, estacionamientos, campamentos y callejones.
- PNG separados para suelos, edificios, fachadas, chatarra y mobiliario urbano.
- Los edificios, coches y obstáculos grandes tienen colisión circular-contra-rectángulo.
- Solo se dibuja lo que está dentro del rectángulo visible de la cámara (con margen); mapa y bloques cargados a demanda.
- Se mantienen el protagonista, su sprite animado, vida, invulnerabilidad, teclado y cámara de 0.4.

## Archivos importantes

- `core/src/main/java/com/deadbeat/world/CityMap.java`: calles, parcelas, edificios, distritos y colisiones.
- `core/src/main/java/com/deadbeat/world/CityRenderer.java`: dibujo PNG del suelo, fachadas, objetos y aceras.
- `core/src/main/java/com/deadbeat/world/GameWorld.java`: integra mundo y jugador.
- `core/src/main/java/com/deadbeat/screens/GameScreen.java`: cámara y HUD.
- `assets/world2/`: PNG utilizados por el mundo. Los materiales del suelo están en `surfaces/` y se repiten sin costuras; ya no se usa el antiguo atlas `ground.png`.
- `assets/sprites/player_walk.png`: sprite animado del jugador (sin cambios de animación).
- `docs/WORLD_0_5.md`: guía técnica, parámetros y pruebas.

## Nota sobre el repositorio

El código base para este ZIP es la copia verificable de Deadbeat 0.4 proporcionada en la conversación. No se modificó directamente `Klonoh/Deadbeat`, porque no fue posible obtener la rama actual desde GitHub. Antes de fusionar, comparar las diferencias con los cambios más recientes del equipo.

## Verificación

Los archivos Java se compilaron con Java 11 mediante dobles de prueba de las APIs de LibGDX y se superaron 29 pruebas de lógica. La compilación con dependencias oficiales y el renderizado real de LWJGL3 quedan pendientes de ejecución local porque este entorno no pudo descargar Gradle.
