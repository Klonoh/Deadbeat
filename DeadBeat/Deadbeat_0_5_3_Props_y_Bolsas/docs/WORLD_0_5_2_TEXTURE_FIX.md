# Deadbeat 0.5.2 — Texturas ancladas al mundo

## Problema corregido
En la versión 0.5.1, el material del suelo se dibujaba como un único
rectángulo recortado por la cámara, modificando en cada fotograma las
coordenadas UV de un `TextureRegion`. Al caminar podían producirse
movimientos aparentes del patrón y saltos en los bordes del pavimento.

## Solución
- El mapa conserva la misma geometría, calles, edificios, decoración y colisiones.
- Las texturas de suelo de 64 × 64 píxeles se dibujan **sobre una cuadrícula fija
  de coordenadas del mundo**, independiente del jugador y de la cámara.
- `CityRenderer.drawMaterial()` calcula la baldosa absoluta de cada punto.
  No utiliza UV fraccionarias que cambian con la cámara, ni `TextureWrap.Repeat`.
- Los bordes de materiales se recortan con coordenadas enteras de textura.
- Las marcas viales continúan calculándose desde `CityMap`, sin cambios.
- El personaje mantiene WASD, animaciones, cámara centrada, 185 u/s y colisiones.

## Cómo comprobarlo en el juego
1. Camina por una calle con grietas visibles y deja de moverte.
2. Acércate lentamente a una grieta: debe conservar su posición respecto de
   los edificios, bordillos y otras marcas fijas del mundo.
3. Cambia de dirección, camina en diagonal y vuelve a la misma posición.
   Las grietas y baldosas deben permanecer en los mismos sitios.
4. Cruza varios límites entre baldosas y entra a un callejón: no deberían
   aparecer cortes móviles ni patrones que se deslicen bajo edificios.
5. Si redimensionas la ventana, las texturas no deben cambiar de fase.

**Aclaración:** cuando la cámara está centrada en el jugador, los edificios y
el suelo **sí se desplazan en la pantalla en dirección contraria** al caminar.
Eso es normal; lo que no debe ocurrir es que los *dibujos de las texturas*
se muevan respecto de edificios, bordillos y coordenadas fijas del mundo.

## Cambios realizados
- `core/src/main/java/com/deadbeat/world/CityRenderer.java`
- `core/src/test/java/com/deadbeat/world/CityRendererTileTest.java`
- `docs/WORLD_0_5_2_TEXTURE_FIX.md`

## Verificación
Se incluye una prueba de regresión para la alineación de las baldosas en
coordenadas del mundo. La ejecución gráfica real de LibGDX debe comprobarse
en un computador con el entorno y dependencias del proyecto instalados.
