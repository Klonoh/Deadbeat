# DEADBEAT - Prototipo 0.2 (Player)

Survivors-like de zombis, estetica punk y armas-instrumento. Este ZIP es la **primera entrega jugable del movimiento del jugador**. Se basa en la estructura 0.1 (`DeadbeatGame`, `GameScreen`, `GameWorld`) y anade `Player` y `Damageable`.

## Herramientas

- **Java JDK 11** (compilacion orientada a Java 11).
- **Gradle Wrapper 7.6.4** (incluido: no necesitas instalar Gradle aparte).
- **LibGDX 1.14.2**, backend de escritorio LWJGL3.
- Internet la primera vez para descargar Gradle, LibGDX y JUnit.

## Ejecutar en Windows (PowerShell)

Desde la carpeta raiz que contiene `gradlew.bat`:

```powershell
java -version
.\gradlew.bat :core:compileJava :lwjgl3:compileJava
.\gradlew.bat :lwjgl3:run
```

En Eclipse/NetBeans, importa como **proyecto Gradle** y ejecuta `lwjgl3 > run`. Selecciona **Gradle Wrapper**, no una version global como Gradle 8.3.

## Controles

| Entrada | Funcion |
|---|---|
| **WASD** o **flechas** | Mover al personaje en ocho direcciones |
| **H** | Aplicar 20 puntos de dano (control de prueba) |
| **R** | Reiniciar posicion, vida, velocidad, inmunidad y tiempo |
| **ESC** | Cerrar juego |

`H` y `R` son controles de depuracion **temporales** y no son parte del gameplay definitivo.

El personaje esta representado provisionalmente por una figura punk construida a partir de rectangulos de colores. No necesitamos assets externos para esta version.

## Comportamiento implementado

- Posicion en el **centro** del jugador, radio de colision 14 unidades.
- Velocidad 185 unidades/segundo, independiente de FPS usando `delta`.
- Movimiento diagonal normalizado: W + D NO acelera respecto de W.
- Limites de arena que consideran el radio completo; el jugador no cruza muros.
- Vida 100/100; `Damageable.takeDamage()`; muerte a HP 0 y bloqueo del movimiento.
- Invulnerabilidad de 0.65 segundos al recibir dano; indicador y parpadeo visual.
- `Player.update(delta, horizontal, vertical)` permite probar logica sin ventana.
- Una unica textura 1x1 en `GameWorld`, reutilizada para arena y jugador.
- Manejo centralizado de recursos (`dispose`) para `Texture`, `SpriteBatch` y `BitmapFont`.

## Prueba manual propuesta

1. Abre el juego: se ve la arena y un pequeno punk en el centro.
2. Mueve con WASD o flechas; prueba diagonales y soltar todas las teclas.
3. Ve a los cuatro bordes y observa que el personaje no puede salir.
4. Pulsa **H** una vez: 100 -> 80 y aparece `INVULNERABLE`.
5. Pulsa **H** inmediatamente: permanece 80. Espera 0.65 s y pulsa otra vez: 80 -> 60.
6. Pulsa **H** con suficiente separacion entre pulsaciones hasta llegar a cero: deja de moverse.
7. Pulsa **R**: vuelve al centro con 100 HP y temporizador a cero.
8. Redimensiona la ventana: la imagen conserva proporcion 16:9.

## Pruebas automatizadas

```powershell
.\gradlew.bat :core:test
```

Incluye pruebas JUnit4 de normalizacion, delta, limites, vida, invulnerabilidad, reinicio y validaciones. No necesitan iniciar una ventana. (JUnit se descarga automaticamente la primera vez.)

## Clases clave

```text
core/src/main/java/com/deadbeat/
├── DeadbeatGame.java
├── entities/
│   ├── Damageable.java     <- interfaz reusable por Player/Zombie
│   └── Player.java         <- atributos privados, movimiento, vida y estado
├── screens/
│   └── GameScreen.java     <- HUD, entrada de prueba, ventana y renderizado
└── world/
    └── GameWorld.java     <- limites de la arena y dibujo del protagonista
```

## Ajustes para tus pruebas

Edita en `Player.java`:
- `DEFAULT_SPEED` para desplazamiento (actual: 185).
- `DEFAULT_MAX_HEALTH` para vida maxima (actual: 100).
- `DEFAULT_RADIUS` para hitbox y distancia a muros (actual: 14).
- `INVULNERABILITY_SECONDS` (actual: 0.65).

Se usa `getX()/getY()` para posicion, `getRadius()` para colisiones futuras, `setMoveSpeed()` para una mejora futura y `Damageable` para permitir aplicar dano a jugadores y zombies sin conocer sus clases.

**No incluidos aun:** zombies, ataques, armas, experiencia, oleadas y menu definitivo. Los controles H/R solo sirven para validar `Player` antes de la integracion.

## Siguiente etapa

Programador 2: agregar `Zombie` y `MovementStrategy`, luego conectar la colision por distancia al cuadrado con `Player` en `GameWorld`.
