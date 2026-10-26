# DEADBEAT — Prototipo 0.4 · Ciudad explorable

Videojuego Java 11 con LibGDX 1.14.2 y Gradle Wrapper 7.6.4.

**Esta versión incorpora una ciudad postapocalíptica navegable con calles, aceras, edificios, vehículos abandonados, cámara que sigue al jugador y colisiones físicas.** Conserva el personaje punk animado en ocho direcciones de Deadbeat 0.3.1.

## Ejecutar en Windows

1. Extraer el ZIP completo (no abrir el juego directamente desde el ZIP).
2. Instalar / seleccionar JDK 11 en el IDE si no está configurado.
3. Abrir PowerShell dentro de `Deadbeat`.
4. Ejecutar:

```powershell
.\gradlew.bat :lwjgl3:run
```

El primer arranque puede descargar Gradle y LibGDX mediante internet. Para ejecutar pruebas de lógica:

```powershell
.\gradlew.bat :core:test
```

## Controles

| Tecla | Acción |
|---|---|
| WASD / flechas | Caminar (8 direcciones) |
| H | Quitar 20 HP (prueba) |
| R | Reiniciar personaje y cronómetro |
| ESC | Cerrar juego |

## Qué probar primero

- Caminar 15–30 segundos por una calle: ver que el escenario sigue extendiéndose y el personaje permanece centrado.
- Moverse contra los edificios, pasar por las aceras, rodear automóviles y comprobar que no puedes atravesarlos.
- Avanzar en diagonal hacia una pared para comprobar que el jugador se desliza por ella.
- Regresar sobre tus pasos y verificar que el barrio no cambia.
- Probar daño (`H`) y reinicio (`R`).

## Estructura principal

- `core/.../entities/Player.java`: movimiento, vida, orientación, colisiones y estado de animación.
- `core/.../world/CollisionMap.java`: contrato para colisiones de entidades.
- `core/.../world/CityMap.java`: geometría y bloques de ciudad deterministas.
- `core/.../world/CityRenderer.java`: renderizado de la región visible de la ciudad.
- `core/.../world/GameWorld.java`: estado de simulación e integración.
- `core/.../screens/GameScreen.java`: cámara que sigue al jugador, dos viewports y HUD.
- `core/.../graphics/PlayerSpriteRenderer.java`: animación de caminar.
- `assets/world/city_tiles.png`: atlas de suelo.
- `assets/sprites/player_walk.png`: spritesheet transparente del protagonista.

**Nota**: todavía no hay enemigos ni instrumentos musicales; el objetivo actual es probar la navegación y el nuevo mundo.
