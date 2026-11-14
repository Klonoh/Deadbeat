# REPORTE EJECUTIVO DE CUMPLIMIENTO DE REQUERIMIENTOS

**Proyecto Videojuego (GM) – DEADBEAT**

Integrantes:  
Sebastián Arriagada  
Joaquín Carrillo  
Benjamín Beltrán

# GM-1: Explicación del juego desde la perspectiva del jugador

**Requerimiento:** Explicar el juego desde la perspectiva del jugador, incluyendo objetivos, narrativa, elementos y controles, utilizando maquetas o pantallazos.

Deadbeat presenta una experiencia de supervivencia en una ciudad postapocalíptica. Desde la perspectiva del jugador, el objetivo consiste en controlar al protagonista, desplazarse por la arena, evitar a los enemigos y resistir las cinco oleadas. La partida termina en derrota cuando el personaje pierde toda su vida. La victoria se obtiene cuando finaliza la quinta oleada y ya no quedan zombies activos.

La narrativa plantea que el último músico de una ciudad destruida utiliza sus instrumentos para abrirse paso entre hordas de muertos vivientes. Esta premisa se refleja en la identidad del juego y en los elementos que aparecen durante la partida.

Los elementos principales son el protagonista, los zombies de distintos tipos (normal, rápido y tanque), el escenario urbano, los objetos de experiencia que aparecen al derrotar enemigos y los indicadores de estado de la partida. El personaje cuenta con movimiento en ocho direcciones mediante WASD o las flechas del teclado. En la pantalla principal se puede seleccionar JUGAR o SALIR mediante teclado o mouse. Durante la partida, ESC permite pausar o reanudar y R permite reiniciar la partida. Q y E permiten cambiar la pista musical.

El juego incorpora un menú principal, una pantalla de partida y paneles superpuestos para pausa y resultado. También incluye información visual de la vida del jugador y elementos de música. La experiencia y el nivel del jugador se administran en la clase Player; la generación y recolección de orbes se coordinan desde GameWorld.

## Evidencia en el código fuente

* MainMenuScreen.java: menú principal, opciones JUGAR/SALIR y controles de selección (aprox. líneas 21–29 y 98–162).  
* GameScreen.java: controles de pausa, reinicio, actualización y resultado de la partida (aprox. líneas 137–195 y 205–216).  
* Player.java: movimiento mediante WASD/flechas, vida y experiencia (aprox. líneas 106–127, 187–198 y 47–70).  
* GameWorld.java: oleadas, aparición de enemigos, experiencia y condición de finalización (aprox. líneas 166–195 y 287–299).

PANTALLAZOS DEL JUEGO

# GM-2: Análisis desde la perspectiva de ingeniería de software

**Requerimiento:** Analizar el juego desde la perspectiva de ingeniería de software, especificando el juego base y las modificaciones propuestas.

El proyecto toma como referencias de diseño los videojuegos SpaceNav2024 y GameLluviaMenu2024, identificados en la hoja de ruta del equipo. SpaceNav2024 se considera una referencia para estudiar aspectos de movimiento, proyectiles y colisiones, mientras que GameLluviaMenu2024 sirve como referencia para la organización de pantallas y objetos. Estas referencias se utilizan como punto de comparación conceptual y no como justificación para reutilizar código de terceros.

A partir de esas referencias, Deadbeat propone una experiencia propia centrada en la supervivencia por oleadas. Entre las modificaciones y decisiones de diseño se encuentran la ambientación postapocalíptica, el desplazamiento del jugador en una arena urbana, tres perfiles de zombies, dos instrumentos musicales como elementos de combate dentro del juego, generación automática de ataques, recolección de experiencia y configuración de cinco oleadas mediante Builder.

Desde la ingeniería de software, el proyecto separa responsabilidades entre las pantallas, el mundo de juego, las entidades, las estrategias de movimiento, los elementos de combate y la configuración de oleadas. Por ejemplo, GameScreen administra la interacción visual y el ciclo de renderizado, mientras que GameWorld coordina entidades, colisiones, aparición de enemigos, experiencia y avance de oleadas. Player y Zombie mantienen su propio estado y comportamiento.

Esta separación reduce la concentración de lógica en una sola clase y facilita extender el juego. La incorporación de MovementStrategy permite variar el comportamiento de desplazamiento sin modificar la estructura general de Zombie. De forma similar, Weapon centraliza el ciclo común de actualización de los ataques y deja que sus implementaciones concretas definan la acción específica.

## Evidencia en el código fuente

* DEADBEAT\_RUTA\_DESARROLLO.md: matriz de trazabilidad GM-2, donde se identifican SpaceNav2024 y GameLluviaMenu2024 como referencias.  
* DeadbeatGame.java: inicialización de la aplicación y selección de la pantalla inicial.  
* GameScreen.java: gestión de la pantalla de juego y de la interacción visual.  
* GameWorld.java: coordinación del mundo y de sus entidades.  
* Player.java y Zombie.java: comportamiento y estado de las entidades.  
* MovementStrategy.java, ChaseMovement.java y ZigzagMovement.java: separación de algoritmos de movimiento.

TABLA COMPARATIVA CONCRETA CON CAPTURAS DE LOS JUEGOS REFERENCIAS Y DEADBEAT

# GM-3: Diseño UML de clases del dominio

**Requerimiento:** Diseñar un diagrama UML con clases del dominio del juego (clases base LibGDX y clases propias) y su implementación en Java.

La estructura del proyecto combina clases proporcionadas por LibGDX con clases propias que representan la lógica del videojuego. DeadbeatGame hereda de Game y actúa como punto de entrada de la aplicación. MainMenuScreen y GameScreen heredan de ScreenAdapter para implementar las pantallas. GameScreen mantiene una instancia de GameWorld, que coordina el escenario y las entidades de la partida.

Dentro del dominio, Player y Zombie implementan la interfaz Damageable, que define las operaciones comunes para recibir daño y consultar si una entidad continúa activa. Zombie mantiene una referencia a MovementStrategy, implementada por ChaseMovement y ZigzagMovement. Player mantiene una colección de objetos Weapon, cuya clase abstracta es extendida por ElectricGuitar y Drumsticks. Finalmente, WaveBuilder construye objetos Wave que contienen la configuración de cada oleada.

## Evidencia en el código fuente

* DeadbeatGame.java: herencia desde com.badlogic.gdx.Game.  
* MainMenuScreen.java y GameScreen.java: herencia desde ScreenAdapter.  
* GameWorld.java: composición y coordinación de Player, Zombie, Projectile, ExperienceOrb y Wave.  
* Player.java y Zombie.java: implementación de Damageable.  
* MovementStrategy.java, ChaseMovement.java y ZigzagMovement.java: interfaz y estrategias concretas.  
* Weapon.java, ElectricGuitar.java y Drumsticks.java: clase abstracta y especializaciones.  
* Wave.java y WaveBuilder.java: configuración inmutable de oleadas y construcción.

UML

# GM-4: Clase abstracta e interfaz

**Requerimiento:** Diseñar y codificar una clase abstracta que sea padre de al menos dos clases y sea utilizada en el programa, además de una interfaz implementada por al menos dos clases y utilizada en el programa.

El proyecto utiliza la clase abstracta Weapon como base para representar el comportamiento compartido de los elementos de combate del jugador. Esta clase concentra atributos comunes, como el daño y el intervalo de actualización, y define el método abstracto performAttack(), que obliga a cada subclase a implementar su propia acción.

A partir de Weapon se implementan ElectricGuitar y Drumsticks. Ambas se incorporan al equipamiento del jugador desde Player.reset() y se actualizan mediante updateWeapons(), que recorre la colección utilizando el tipo base Weapon. De esta manera, la abstracción no queda solamente declarada, sino que participa en el funcionamiento del juego.

También se define la interfaz Damageable, que establece los métodos takeDamage() e isAlive(). Las clases Player y Zombie implementan esta interfaz, ya que ambas entidades pueden recibir daño y dejar de estar activas. GameWorld utiliza referencias de tipo Damageable en su método de aplicación de daño, lo que permite tratar a ambas clases mediante un contrato común.

## Evidencia en el código fuente

* Weapon.java: declaración de la clase abstracta y método abstracto performAttack().  
* ElectricGuitar.java y Drumsticks.java: clases que extienden Weapon.  
* Player.java: implementa Damageable, mantiene la colección de Weapon y actualiza sus elementos.  
* Damageable.java: declaración de la interfaz.  
* Zombie.java: implementa Damageable.  
* GameWorld.java: método damage(Damageable target, float amount), que utiliza la interfaz.

# GM-5: Encapsulamiento y principios de orientación a objetos

**Requerimiento:** Aplicar encapsulamiento y principios de orientación a objetos en el diseño y la implementación del videojuego.

El proyecto aplica encapsulamiento mediante atributos privados en sus clases principales. En Player, por ejemplo, la posición, la velocidad, la vida, la experiencia, el nivel y la colección de elementos equipados se mantienen como estado interno. El acceso a estos datos se realiza mediante métodos públicos que exponen la información necesaria para el resto del sistema.

Zombie también mantiene privados sus atributos de estado, como el tipo, la posición, la vida y la estrategia de movimiento. En lugar de permitir que otras clases modifiquen directamente su estado interno, ofrece métodos para actualizar su comportamiento, recibir daño y consultar sus características.

Se aplican además principios de orientación a objetos mediante abstracción, herencia, composición, interfaces y polimorfismo. Weapon concentra el comportamiento común de las implementaciones concretas; Damageable define un contrato compartido por Player y Zombie; y MovementStrategy permite delegar el comportamiento de movimiento en objetos especializados.

La distribución de responsabilidades también contribuye a la modularidad: GameScreen se encarga de la presentación y entrada de usuario, GameWorld coordina la simulación y las entidades administran su propio estado. En Player, getWeapons() entrega una vista no modificable de la colección, evitando que otros componentes alteren directamente el equipamiento.

## Evidencia en el código fuente

* Player.java: atributos privados (aprox. líneas 27–45), métodos de acceso y colección de armas no modificable (líneas 73–80 y 238–248).  
* Zombie.java: atributos privados y acceso controlado a su estrategia (aprox. líneas 15–23 y 115–121).  
* GameWorld.java: colecciones privadas de zombies, proyectiles y orbes; getters que entregan vistas no modificables.  
* Weapon.java: abstracción del comportamiento común.  
* Damageable.java y MovementStrategy.java: contratos mediante interfaces.

# GM-6: Patrón de diseño Singleton

**Requerimiento:** Aplicar el patrón Singleton, describiendo el problema, contexto, solución y UML.

El patrón Singleton se propone para centralizar el estado de una única sesión de juego. El problema que busca resolver es la posibilidad de que distintas pantallas o componentes mantengan referencias a estados diferentes de la misma partida, por ejemplo, oleada actual, cantidad de enemigos derrotados o resultado final.

En el contexto de Deadbeat, la aplicación es monojugador y se ejecuta una sola partida activa a la vez. Por ello, una instancia compartida de GameSession permitiría mantener un estado coherente entre las pantallas y reiniciarlo al comenzar una nueva partida.

## Evidencia en el código fuente

* GameScreen.java: enum interno RunOutcome y estado local de la partida (aprox. líneas 52–97).  
* GameScreen.java: creación y reinicio del mundo mediante GameWorld (aprox. líneas 98–135 y 205–217).  
* DEADBEAT\_RUTA\_DESARROLLO.md: apartado de Singleton y estados, donde se especifica GameSession como implementación planificada.

**Requerimiento:** Aplicar el patrón Strategy, describiendo el problema, contexto, solución y UML.

El patrón Strategy se utiliza para permitir que los zombies tengan distintos comportamientos de desplazamiento sin concentrar todos los algoritmos en una única clase. El problema consiste en que los distintos tipos de enemigos necesitan moverse de manera diferente y, además, el comportamiento de algunos puede cambiar durante la partida.

Para resolverlo, se define la interfaz MovementStrategy, que establece el método move(). Las clases ChaseMovement y ZigzagMovement implementan ese contrato con algoritmos distintos. Zombie mantiene una referencia a MovementStrategy y delega en ella el desplazamiento.

La estrategia se asigna al crear el zombie según su tipo. Los zombies normales y tanque utilizan persecución directa, mientras que los rápidos utilizan zigzag. Además, Zombie puede alternar la estrategia del enemigo rápido durante la ejecución, cambiando entre ZigzagMovement y ChaseMovement sin reemplazar la clase Zombie.

Esta solución evita que la lógica de movimiento dependa de una gran cantidad de condicionales dentro de una sola clase y facilita incorporar nuevas estrategias sin modificar la estructura principal del enemigo.

**Evidencia en el código fuente**

* MovementStrategy.java: interfaz con el método move().  
* ChaseMovement.java: implementación de persecución directa.  
* ZigzagMovement.java: implementación de desplazamiento zigzagueante.  
* Zombie.java: atributo strategy, asignación inicial y cambio de estrategia durante update() (aprox. líneas 23 y 41–50; métodos setMovementStrategy() y getMovementStrategy()).

FALTA CLASE GAMESSION SINGLETON 

# GM-7: Patrón de diseño Strategy

**Requerimiento:** Aplicar el patrón Strategy, describiendo el problema, contexto, solución y UML.

El patrón Strategy se utiliza para permitir que los zombies tengan distintos comportamientos de desplazamiento sin concentrar todos los algoritmos en una única clase. El problema consiste en que los distintos tipos de enemigos necesitan moverse de manera diferente y, además, el comportamiento de algunos puede cambiar durante la partida.

Para resolverlo, se define la interfaz MovementStrategy, que establece el método move(). Las clases ChaseMovement y ZigzagMovement implementan ese contrato con algoritmos distintos. Zombie mantiene una referencia a MovementStrategy y delega en ella el desplazamiento.

La estrategia se asigna al crear el zombie según su tipo. Los zombies normales y tanque utilizan persecución directa, mientras que los rápidos utilizan zigzag. Además, Zombie puede alternar la estrategia del enemigo rápido durante la ejecución, cambiando entre ZigzagMovement y ChaseMovement sin reemplazar la clase Zombie.

Esta solución evita que la lógica de movimiento dependa de una gran cantidad de condicionales dentro de una sola clase y facilita incorporar nuevas estrategias sin modificar la estructura principal del enemigo.

## Evidencia en el código fuente

* MovementStrategy.java: interfaz con el método move().  
* ChaseMovement.java: implementación de persecución directa.  
* ZigzagMovement.java: implementación de desplazamiento zigzagueante.  
* Zombie.java: atributo strategy, asignación inicial y cambio de estrategia durante update() (aprox. líneas 23 y 41–50; métodos setMovementStrategy() y getMovementStrategy()).

UML Y CAPTURA O VIDEO DE LOS COMPORTAMIENTOS

# GM-8: Patrón de diseño Template Method

**Requerimiento:** Aplicar el patrón Template Method, describiendo el problema, contexto, solución y UML.

El patrón Template Method se utiliza para definir una secuencia común de actualización para los elementos de combate del jugador, manteniendo la posibilidad de que cada implementación ejecute una acción distinta. El problema aparece porque los distintos elementos comparten la administración del tiempo de espera entre acciones, pero no realizan la misma operación.

La clase abstracta Weapon resuelve este problema mediante el método final update(). Este método descuenta el tiempo restante, comprueba si el elemento puede ejecutar su acción y luego invoca performAttack(). Si la acción se realiza correctamente, reinicia el intervalo de espera. La secuencia general queda definida en la clase padre.

Las subclases ElectricGuitar y Drumsticks implementan performAttack() de manera diferente. La primera busca un zombie cercano y genera un proyectil, mientras que la segunda delega un impacto de alcance cercano en GameWorld. De esta forma, ambas reutilizan el mismo algoritmo general de actualización sin duplicar la administración del intervalo.

El uso de update() como método final evita que las subclases alteren el orden de los pasos comunes y mantiene la variación limitada al método abstracto performAttack().

## Evidencia en el código fuente

* Weapon.java: método final update() y método abstracto performAttack().  
* ElectricGuitar.java: implementación concreta de performAttack().  
* Drumsticks.java: implementación concreta de performAttack().  
* Player.java: updateWeapons() recorre el equipamiento usando el tipo base Weapon.

UML

METODO PLANTILLA Y IMPLEMENTACIONES CONCRETAS

# GM-9: Patrón de diseño Builder

**Requerimiento:** Aplicar el patrón Builder, describiendo el problema, contexto, solución y UML.

El patrón Builder se utiliza para construir las configuraciones de las oleadas del videojuego. Cada oleada requiere definir una duración y cantidades de zombies normales, rápidos y tanque. Construir estas configuraciones mediante una secuencia de parámetros independientes puede dificultar su lectura y validación.

Para resolverlo, se implementó WaveBuilder con métodos encadenables duration(), normal(), fast() y tank(). El método build() valida que la duración sea positiva, que las cantidades no sean negativas y que la configuración contenga una cantidad válida de enemigos. Si los parámetros no cumplen estas condiciones, se lanza IllegalArgumentException.

El resultado de build() es un objeto Wave, que mantiene sus atributos como valores finales. Esto permite que la configuración construida no se modifique accidentalmente después de su creación.

El método estático campaign() construye cinco configuraciones de oleada utilizando WaveBuilder. GameWorld obtiene estas configuraciones al inicializarse y las utiliza para administrar la aparición progresiva de enemigos. Así, Builder participa en la ejecución real del videojuego y no queda como una clase aislada.

## Evidencia en el código fuente

* WaveBuilder.java: métodos encadenables, validación en build() y método campaign() (aprox. líneas 2–43).  
* Wave.java: objeto de configuración con atributos finales y métodos de consulta.  
* GameWorld.java: invocación de WaveBuilder.campaign() al construir el mundo (aprox. líneas 42–53) y administración del avance de oleadas (líneas 166–195).

UML e PRUEBA VISUAL

**GM-10: Utilización de GitHub** 

**Requerimiento:** Utilizar GitHub y realizar al menos seis commits durante el desarrollo del proyecto.

El proyecto se encuentra alojado en un repositorio de GitHub, donde se mantiene el código fuente y la documentación asociada a su desarrollo. El historial del repositorio permite observar la evolución del videojuego y los cambios realizados durante su implementación.

En la página principal del repositorio se observan 34 commits, cantidad que supera el mínimo de seis solicitado por el requerimiento. El historial permite revisar los cambios incorporados en el proyecto y constituye evidencia del uso de control de versiones.

**Evidencia en el código fuente**

Repositorio: https\://github.com/Klonoh/Deadbeat

Historial de commits: https\://github.com/Klonoh/Deadbeat/commits/main

![][image1]

[image1]: <data:image/png;base64,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>