# DEADBEAT — 0.5.3: Props corregidos, vallas y bolsas interactivas

**Base:** Deadbeat 0.5.2 (ZIP verificado disponible). Java 11, LibGDX 1.14.2, Gradle Wrapper 7.6.4.

## Nuevas funciones

- Se corrigieron los PNG extraídos de los atlas (`fence`, `crate`, `pallet`, `barrier`, `concrete_barricade`, `planter`, `garbage`, `graffiti_wall`, `tent`, `dumpster`, `barrels`). Ya no contienen piezas de recursos vecinos.
- Las vallas ahora son obstáculos físicos del mapa: no pueden atravesarse con el jugador. Se distribuyen en zonas industriales, estacionamientos y campamentos.
- Las bolsas de basura se rompen automáticamente cuando el jugador pasa por ellas. No detienen el movimiento; después de romperse se muestran con **una de tres variantes PNG** según su ubicación.
- El estado de una bolsa rota persiste al salir del bloque y regresar. `R` reinicia la partida y restaura las bolsas intactas.
- El HUD muestra cuántas bolsas se han roto.

## Ejecutar en Windows

En PowerShell, dentro de esta carpeta:

```powershell
.\gradlew.bat :lwjgl3:run
```

## Prueba manual recomendada

1. Camina hasta un campamento, un estacionamiento o una nave industrial y choca contra una valla. No debes poder atravesarla.
2. Comprueba que la valla y las cajas ya no tienen postes ni pedazos laterales duplicados.
3. Camina encima de una bolsa de basura; debe transformarse inmediatamente en una pila abierta o rasgada.
4. Pasa nuevamente sobre la misma bolsa: no debe regenerarse ni volver a aumentar el contador.
5. Aléjate varias pantallas y vuelve. La bolsa seguirá rota, incluso si el sector se regeneró en la caché.
6. Encuentra más bolsas y comprueba que aparecen tres roturas visuales distintas.
7. Pulsa `R`: las bolsas deben aparecer intactas nuevamente.

**Controles:** WASD o flechas para moverte, H para probar daño, R para reiniciar y ESC para salir.

Detalles técnicos y archivos: `docs/PROPS_0_5_3.md`.

> Esta versión fue preparada desde el ZIP 0.5.2, no desde la rama actual de `Klonoh/Deadbeat`, porque la consulta de GitHub no devolvió el contenido. No reemplaces cambios más recientes del equipo sin comparar primero.

---

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
