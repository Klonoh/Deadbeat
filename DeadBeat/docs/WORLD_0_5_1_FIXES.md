# Reparación de baldosas, marcas viales y sprites — 0.5.1

## Problema de origen

El atlas anterior se había recortado en piezas de 64 × 64 píxeles que
incluían bordes y transiciones. Esas piezas se repetían en cada celda
sin conectividad: el asfalto, los pasos de peatones y las marcas se
cortaban. La selección de superficie por el centro de cada celda también
convertía la geometría de calles en dientes de 64 píxeles.

El renderizador aplicaba además filtrado `Nearest` sobre objetos en
coordenadas fraccionarias, mientras la cámara seguía la posición exacta
del jugador. La parte visible sufría pequeños saltos de muestreo.

## Solución

1. Siete PNGs de 64 × 64 píxeles repetibles de material (asfalto, acera,
   ladrillo, callejón, aparcamiento, tierra y tejado).
2. `CityRenderer.drawGround` dibuja asfalto continuo y, sobre él, las
   áreas de las manzanas según los límites EXACTOS de las calles.
3. `drawMaterial` utiliza `TextureWrap.Repeat` y UV ancladas en las
   coordenadas del mundo; no hay estiramiento de las baldosas al recortar.
4. `drawRoadEdges` dibuja bordillos, líneas centrales discontinuas y
   pasos peatonales por carretera y por cruce, no por celda.
5. `GameScreen.followPlayer` redondea la posición de la cámara a un
   píxel; el render de `PlayerSpriteRenderer` y de su sombra también.
6. La lógica del jugador conserva valores `float`, por lo que la
   precisión del movimiento y las colisiones no se degrada.

## Archivos

- `core/.../world/CityRenderer.java`: terrenos y trazado vial.
- `core/.../screens/GameScreen.java`: cámara alineada a píxel.
- `core/.../graphics/PlayerSpriteRenderer.java`: sprite alineado a píxel.
- `core/.../world/GameWorld.java`: sombra alineada a píxel.
- `assets/world2/surfaces/*.png`: materiales editables y repetibles.

## Pruebas manuales

- Recorrer varias cuadras en horizontal y vertical y comprobar que las
  rayas de calzada siguen el eje de su calle sin cortarse en cuadros.
- Entrar a una intersección y revisar los cuatro pasos de peatones.
- Acercarse a edificios y verificar aceras continuas y bordes rectos.
- Mantener movimiento diagonal; el personaje debe verse estable.
- Ir y volver a una manzana; los patrones no deben cambiar de fase.
- Cambiar el tamaño de la ventana: `FitViewport` mantiene proporción.

**Nota:** El juego debe probarse con la ejecución gráfica real de LibGDX
para confirmar el resultado visual en el equipo de desarrollo.
