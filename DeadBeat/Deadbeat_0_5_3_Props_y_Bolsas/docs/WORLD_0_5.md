# DEADBEAT — Escenarios urbanos 0.5

## 1. Objetivo

Se remplazó la repetición de manzanas cuadradas de 0.4 por un mundo de ciudad con calles continuas de anchos variables, distintas fachadas e identidad urbana punk postapocalíptica. La geometría y el renderizado siguen separados para preservar los límites de colisión.

## 2. ¿Cómo funciona la generación?

- `CityMap` emplea una semilla fija y coordenadas del mundo; explorar y volver a una zona devuelve el mismo escenario.
- Las avenidas transcurren aproximadamente cada **816 unidades** y las calles transversales cada **688 unidades**. Los centros se desplazan ligeramente de manera reproducible; los anchos también cambian.
- Entre una avenida y otra, y entre dos calles, se crean parcelas. Su orientación, tamaño de edificios y tipo de ocupación dependen de un hash determinista.
- Existen seis `District`: `SHOPS` (tiendas), `RESIDENTIAL`, `INDUSTRIAL`, `PARKING`, `CAMP` y `BACKSTREET`.
- Los sectores contienen pasillos entre edificios, pequeñas plazas, zonas abiertas, aparcamientos, maquinaria, campamentos y callejones con contenedores y basura.
- Los PNG decorativos se insertan como `Decoration`, sin bloquear movimientos. Las construcciones, coches y otros objetos importantes se agregan como `Obstacle`, con colisión.
- `CityMap` mantiene hasta 96 sectores en una caché LRU. Se reconstruyen automáticamente si se vuelven a visitar después de haber sido expulsados de la caché.

## 3. Renderizado

`CityRenderer` dibuja capas de forma ordenada:

1. Suelo modular: asfalto, aceras, ladrillo, hormigón, tierra y callejones.
2. Pintura vial, bordillos e intersecciones con pasos peatonales.
3. Decoración no sólida: papeles, charcos, hierbas, bancos, faroles, mobiliario, grafitis.
4. Obstáculos y edificios: tejados, fachadas, autos, barreras, basura grande, contenedores y carpas.
5. `GameWorld` dibuja al jugador encima de las capas del mundo.

Las texturas se cargan **una sola vez** al crear `CityRenderer`, usando filtrado `Nearest` para preservar el pixel art; se liberan en `dispose()`.

## 4. Cambiar la ciudad

| Objetivo | Archivo / parámetro |
|---|---|
| Separación entre avenidas | `CityMap.STREET_X` (816) |
| Separación entre calles | `CityMap.STREET_Y` (688) |
| Ancho de avenidas | `CityMap.avenueHalf(int)` |
| Ancho de calles transversales | `CityMap.streetHalf(int)` |
| Tamaños y posición de construcciones | `CityMap.generateBlock(int,int)` |
| Probabilidad / variedad de sectores | Selección `District` en `generateBlock` |
| Zonas transitables o sólidas | `CityMap.collidesCircle(...)` y lista de `Obstacle` |
| Baldosas de suelos | `assets/world2/ground.png` y `CityRenderer.drawGround(...)` |
| Gráficos de autos, fachadas, grafitis, etc. | PNG individuales en `assets/world2/` y `CityRenderer.drawObstacle/drawDecoration` |
| Aspecto de tejados | `CityRenderer.drawBuilding(...)` |
| Cámara y resolución | `GameScreen.followPlayer()` y `VIEW_WIDTH/VIEW_HEIGHT` |

**Importante:** modificar la posición de un edificio requiere cambiar `CityMap`, no solo su PNG, para que la colisión coincida con lo visible. Las fachadas no son entradas jugables todavía.

## 5. Contratos de colisión

- `Player` mantiene un círculo de radio 14 unidades.
- `CityMap` verifica el círculo contra rectángulos de obstáculos cercanos.
- Se resuelven los movimientos X e Y por separado; caminar diagonalmente junto a una pared conserva el deslizamiento lateral.
- Las carreteras principales están despejadas, incluso cuando hay autos aparcados al costado.

## 6. Escenarios implementados

- **Calles comerciales:** dos escaparates de diferentes anchos, callejón lateral, bancas, máquina expendedora y contenedor.
- **Residencial:** viviendas de diferentes dimensiones y posiciones, patios, vegetación y mobiliario.
- **Industrial:** depósitos y talleres, portones, pallets, cajas y barriles.
- **Estacionamiento:** coches abandonados, barreras, neumáticos, zona asfaltada despejada.
- **Campamento:** carpas, chatarra, basura, cercos y espacios de tierra.
- **Callejones:** edificios estrechos enfrentados, pasaje intermedio, charcos, grafitis, contenedores y basura.

## 7. Prueba manual

1. Ejecutar `:lwjgl3:run` y confirmar que carga la imagen del jugador y las nuevas texturas.
2. Caminar al menos cinco pantallas hacia arriba/derecha y revisar los anchos variables.
3. Comprobar continuidad de calles, giros y cruces, sin bordillos atravesando los pasos de peatones.
4. Recorrer un callejón y verificar que se puede pasar entre edificios, pero no atravesarlos.
5. Chocar con autos, contenedores y barricadas; deben detener el movimiento.
6. Volver a una esquina visitada: los edificios y objetos deben estar en el mismo lugar.
7. Moverse diagonalmente contra paredes y verificar el deslizamiento.
8. Probar `H`, `R`, `ESC` y el redimensionamiento de la ventana.
9. Probar una partida con muchos cambios de sectores, observando fluidez y ausencia de fugas de memoria.

## 8. Alcance y límites

Esta versión se centra en el **mundo visual y la exploración**. No incorpora zombies, combate, interiores de edificios, interacción con puertas ni destructibilidad. El personaje puede explorar por superficies transitables y rodear obstáculos, pero no entrar en las fachadas. El resultado debe comprobarse en el equipo local con Gradle y LibGDX reales.
