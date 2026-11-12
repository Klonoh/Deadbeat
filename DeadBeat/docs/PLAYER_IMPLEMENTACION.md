# DEADBEAT 0.2 — Player: implementación y verificación

## Qué está terminado

- [x] **DB-101:** `entities/Damageable.java` implementa contrato `takeDamage` / `isAlive`.
- [x] **DB-102:** `entities/Player.java` mantiene posicion, hitbox circular, vida y velocidad con atributos privados.
- [x] **DB-103:** teclas WASD o flechas leidas desde `Player.update(delta)`.
- [x] **DB-104:** diagonal normalizada con longitud euclidiana; sin ventaja de movimiento.
- [x] **DB-105:** movimiento con `delta`; limites que incluyen radio de 14 unidades.
- [x] **DB-106:** recibir daño, invulnerabilidad durante 0,65 s, vida limitada a [0, max], muerte.
- [x] **DB-107:** figura punk provisional construida con la misma textura compartida de `GameWorld`.

## Pruebas realizadas durante la generación del proyecto

- Se compilaron todos los archivos Java bajo `--release 11` **usando interfaces de prueba** equivalentes a las APIs LibGDX utilizadas, porque en el entorno de generación no había dependencias de LibGDX descargadas.
- **14 pruebas de lógica de Player** terminaron exitosamente, con entradas simuladas y sin gráficos.
- **No se verificó** el arranque real de LWJGL3/LibGDX, ya que falta la descarga de dependencias en este entorno. Hay que verificarlo en el equipo de desarrollo.

## Pruebas pendientes en tu PC

- [ ] `./gradlew :core:test` (Windows: `.\gradlew.bat :core:test`).
- [ ] `./gradlew :core:compileJava :lwjgl3:compileJava`.
- [ ] `./gradlew :lwjgl3:run`.
- [ ] Verificar movimiento WASD/flechas, incluida diagonal.
- [ ] Comprobar los cuatro limites de la arena.
- [ ] Pulsar H dos veces de inmediato: vida solo baja una vez.
- [ ] Esperar al menos 0,65 s y pulsar H: vida baja otra vez.
- [ ] Pulsar H varias veces (con espera): al morir no se mueve.
- [ ] Pulsar R: restaura 100 HP, posicion central y reinicia el tiempo.
- [ ] Redimensionar la ventana: no debe deformarse el mundo.

## Responsabilidades y contratos

`GameScreen` controla el HUD y los atajos de pruebas H/R. `GameWorld` contiene una instancia de `Player` y reutiliza la textura 1x1 para dibujarla. `Player` contiene solo estado del personaje, calculos de movimiento y vida.

`Player.update(delta)` lee teclado real. `Player.update(delta, horizontal, vertical)` permite testear y agregar gamepad en el futuro. En ambas variantes la posición usa **coordenadas del centro**, y `getRadius()` es la referencia para futuras colisiones circulares de zombies.

**El jugador no debe crear ni liberar texturas**. Un futuro `PlayerRenderer` podrá extraer el dibujo de `GameWorld` sin modificar la lógica de Player.

## Valores ajustables

| Campo de `Player` | Valor | Significado |
|---|---:|---|
| `DEFAULT_SPEED` | 185f | Unidades del mundo recorridas en un segundo |
| `DEFAULT_MAX_HEALTH` | 100f | Vida inicial |
| `DEFAULT_RADIUS` | 14f | Radio usado para colisiones y limites |
| `INVULNERABILITY_SECONDS` | 0.65f | Intervalo entre daños admitidos |

La arena interior se define **solo** en `GameWorld` (`ARENA_LEFT`, `ARENA_BOTTOM`, margenes derecho y superior). No dupliques esos numeros en `Zombie`.

## Próximo trabajo de P2

1. Implementar `Zombie implements Damageable` con X/Y centrales y `getRadius()`.
2. Crear `MovementStrategy` y `ChaseMovement`.
3. En `GameWorld` crear cinco zombies y actualizarlos usando el `Player` actual.
4. Detectar contacto usando distancia al cuadrado y radios combinados; aplicar `player.takeDamage(dano)`.
5. Sustituir las teclas H/R de prueba cuando exista un flujo de partida real.
