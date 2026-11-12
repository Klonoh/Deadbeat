# Deadbeat 0.5.3 — Correcciones de props y bolsas de basura

## 1. Defecto visual corregido

Los archivos PNG anteriores se habían recortado desde un atlas amplio pero contenían también pequeños trozos de los sprites adyacentes. Se revisaron sus componentes alfa y se recortaron exclusivamente los elementos deseados. Esto afecta a `fence.png`, `crate.png`, `pallet.png`, `barrier.png`, `concrete_barricade.png`, `planter.png`, `garbage.png`, `graffiti_wall.png`, `tent.png`, `dumpster.png` y `barrels.png`. No se alteró el renderizado del suelo de la versión 0.5.2.

## 2. Vallas físicas

**Archivo:** `core/src/main/java/com/deadbeat/world/CityMap.java`

- Se incorporan obstáculos `Kind.FENCE` en campamentos, estacionamientos e industria.
- Su rectángulo de colisión queda en las coordenadas del mundo y coincide con la anchura y altura del sprite mostrado.
- `collidesCircle(...)` ya detectaba obstáculos `Kind`; se añadió además una salvaguarda para futuras decoraciones `DecorKind.FENCE`.
- `CityRenderer.java` sigue dibujando el mismo objeto por la misma posición (`drawObstacle`), pero con el PNG reparado.

## 3. Bolsas interactivas

**Archivos:**

- `CityMap.java`: `breakGarbageNear`, `isGarbageBroken`, `brokenGarbageVariant`, `resetGarbage`, `brokenGarbageCount`.
- `GameWorld.java`: activa la rotura después de actualizar la posición del jugador, en cada fotograma válido. El reseteo limpia el estado de las bolsas.
- `CityRenderer.java`: selecciona el PNG correcto, según el estado de cada decoración `TRASH`.
- `GameScreen.java`: muestra un contador sencillo de bolsas rotas.

**PNG nuevos:** `assets/world2/garbage_broken_1.png`, `garbage_broken_2.png`, `garbage_broken_3.png`.

Los cuatro recursos (intacto más tres variantes) tienen dimensiones **77 × 62**, transparencia y la misma base visual para que no haya desplazamientos al sustituirse. Los desgarros y restos varían de una bolsa a otra, y la selección se calcula mediante la semilla del generador de ciudad; no depende del número de fotogramas.

**Persistencia:** `CityMap` almacena identificadores estables (coordenadas del sector e índice de la decoración) en un `HashSet<Long>`, independiente de la caché LRU de bloques. Esta persistencia dura toda la partida en memoria; no hay guardado a disco.

**Colisión:** la basura se detecta mediante intersección de círculo del jugador con el rectángulo de la decoración, pero no es un objeto sólido. Las vallas sí son sólidas.

## 4. Verificación

Se compiló sintácticamente todo el código Java con JDK 11 y dobles locales de LibGDX/JUnit. Pasaron 44 pruebas lógicas, incluyendo ocho pruebas nuevas de interacciones y una prueba de movimiento contra vallas.

Además se comprobaron las dimensiones y transparencia de los PNG corregidos. El inicio gráfico real con dependencias oficiales de LibGDX no pudo verificarse en el entorno de creación; debe probarse ejecutando `:lwjgl3:run` en el equipo de desarrollo.

## 5. Ajustes sugeridos

- Para mover las vallas o cambiar su tamaño: `CityMap.generateBlock` (casos `INDUSTRIAL`, `PARKING` y `CAMP`).
- Para modificar la distancia a la que se rompen las bolsas: `CityMap.breakGarbageNear` y el radio pasado desde `GameWorld.update`.
- Para sustituir las tres variantes visuales: editar los PNG, conservando el tamaño de 77 × 62 y el fondo transparente.
- Para añadir más variantes: cargar los nuevos nombres en `CityRenderer.FILES` y ampliar `brokenGarbageVariant`.
