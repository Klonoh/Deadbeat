# DEADBEAT — 0.3: Player con sprites animados

## Cambios

1. Se reemplazó la figura hecha con rectángulos por el spritesheet punk aprobado.
2. Se extrajo el fondo verde y se exportó `assets/sprites/player_walk.png` transparente.
3. Se definieron 8 direcciones y 3 poses por dirección.
4. La animación camina en secuencia [1, 0, 2, 0], a 0.11 s por fase, y queda quieta al soltar las teclas.
5. El personaje sigue girado hacia su última dirección tras dejar de caminar.
6. Se conserva la lógica de movimiento, la vida, el daño, la invulnerabilidad y la hitbox.
7. El spritesheet se carga una sola vez y se libera junto al mundo al cerrar el juego.
8. Se añadieron pruebas del estado de animación y la correspondencia de las 8 direcciones.

## Archivos

- `core/.../entities/Player.java`: estado de movimiento (`moving`, `walkTime`).
- `core/.../graphics/FacingDirection.java`: conversión vector -> fila del sprite.
- `core/.../graphics/PlayerSpriteRenderer.java`: selección y renderizado del frame.
- `core/.../world/GameWorld.java`: delega la representación visual del jugador.
- `assets/sprites/player_walk.png`: spritesheet transparente de 3 columnas x 8 filas.

## Prueba manual

1. Ejecuta `./gradlew :lwjgl3:run` (Windows: `gradlew.bat`).
2. Mantén W, A, S, D y las cuatro diagonales. El sprite debe girar y caminar.
3. Suelta la tecla; la animación se detiene sin girar de regreso al frente.
4. Mantén dos teclas opuestas; el personaje permanece quieto.
5. Pulsa H; el sprite parpadea mientras la inmunidad dura.
6. Pulsa R; vuelve al centro, mirando hacia abajo e inmóvil.
7. Redimensiona la ventana; el píxel debe conservar bordes nítidos.

## Ajustes

- Velocidad y hitbox: `Player.java` (se mantienen en 185 y radio 14).
- Frecuencia de pasos: `PlayerSpriteRenderer.STEP_SECONDS` (0.11).
- Tamaño del personaje: `PlayerSpriteRenderer.draw()` (64x80).
- Relación de direcciones y filas: `FacingDirection`.

**Nota**: Las pruebas compiladas con stubs verifican Java y lógica. El arranque
real del backend de LibGDX aún requiere ejecutarlo en un entorno gráfico.
