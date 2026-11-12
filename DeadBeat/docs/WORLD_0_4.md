# Deadbeat 0.4 — Ciudad postapocalíptica explorable

## Objetivo

Sustituir la arena fija por un mundo desplazable que se dibuja según la posición del jugador, con calles, edificios, coches abandonados y colisiones.

## Implementación

- **`CityMap.java`**: mapa lógico de 131072 × 131072 unidades (aprox. 136×242 pantallas lógicas), bloques de 512 × 512. Generación determinista con semilla fija, **caché LRU de hasta 96 bloques**. Cada bloque tiene cuatro edificios y dos coches abandonados. El mapa no reserva memoria proporcional a toda su superficie.
- **`CityRenderer.java`**: dibuja únicamente baldosas y obstáculos dentro (o cerca) del rectángulo visible de la cámara; utiliza un atlas de 8 baldosas y texturas con filtrado `Nearest`. Edificios con techos, ventanas y elementos de azotea, carreteras con líneas amarillas, pasos de peatones y aceras. Los detalles decorativos no son obstáculos.
- **`CollisionMap.java`**: interfaz para consultar obstáculos usando un círculo.
- **`Player.java`**: el movimiento se resuelve por separado en los ejes X/Y para permitir desplazamiento junto a muros, sin atravesarlos. La orientación, velocidad, hitbox y lógica de daño se conservan.
- **`GameWorld.java`**: conecta el mapa, el jugador, el renderizador del escenario y el sprite animado.
- **`GameScreen.java`**: cámara que sigue al jugador inmediatamente; `FitViewport` independiente para el HUD estático. La cámara solo deja de centrarlo al llegar a los límites extremos del mapa.

## Controles

- WASD / flechas: mover jugador.
- H: simular 20 puntos de daño.
- R: reiniciar jugador en el punto de origen (la ciudad se mantiene idéntica).
- ESC: salir.

## Uso

En la carpeta `Deadbeat_0_4_World`:

```powershell
.\gradlew.bat :lwjgl3:run
```

Pruebas:

```powershell
.\gradlew.bat :core:test
```

## Criterios de aceptación (prueba manual)

1. El jugador permanece en el centro de la pantalla mientras las calles y edificios pasan a su alrededor (excepto en los extremos lejanos del mundo).
2. El mapa no termina al salir de la zona que cabía en el prototipo anterior.
3. Los edificios y vehículos impiden el paso. Desplazarse diagonalmente contra un muro permite deslizarse por su costado.
4. Al regresar a una zona, los edificios y coches conservan sus posiciones.
5. El sprite se anima en las ocho direcciones, se detiene al soltar teclas y no adquiere píxeles verdes en el pelo.
6. HUD, controles de prueba, vida, reinicio y resolución escalable continúan funcionando.
7. El consumo de memoria de la ciudad no crece indefinidamente por explorar nuevas zonas (caché limitada).

## Límites de esta etapa

- No hay zombies, armas, físicas complejas, interiores ni audio todavía.
- Los edificios se representan por sus azoteas (vista cenital). Se pueden rodear, pero no entrar.
- Los detalles visuales están construidos con arte pixelado simple; el mapa es procedimental, no un mapa dibujado a mano.
- El mundo es muy grande, pero técnicamente **finito**; tiene bordes lejanos que la cámara respeta.
- El arranque gráfico real debe probarse con el backend LibGDX/LWJGL3 y una GPU compatible.
