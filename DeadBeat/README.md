# DEADBEAT

Juego de supervivencia 2D desarrollado con Java, LibGDX y LWJGL3. Un músico punk debe sobrevivir cinco oleadas de zombis mediante ataques automáticos de guitarra eléctrica y baquetas, recoger experiencia y mejorar sus habilidades.

> **Estado de referencia:** commit [`a2f7d4b`](https://github.com/Klonoh/Deadbeat/commit/a2f7d4b1fddcd0d48232e6750be37c6d63d85b02). En este commit finalizaron correctamente los workflows de compatibilidad JDK 11 y empaquetado portable Windows JDK 17. Estos resultados no sustituyen la ejecución manual.

## Características actuales

- Ciudad de desplazamiento libre, cámara centrada y colisiones con edificios y objetos.
- Zombis normales, rápidos y tanque; movimientos de persecución y zigzag mediante Strategy.
- Ataques automáticos con guitarra (proyectiles con sprite animado) y baquetas (efecto de giro).
- Cinco oleadas por tiempo, con 24, 34, 48, 64 y 82 enemigos configurados; después de la quinta debe eliminarse a los zombis restantes para ganar.
- Experiencia, niveles y selección de mejoras; pantallas de menú, pausa, victoria y derrota.
- Música y volumen compartidos entre pantallas; ajustes centralizados en `AudioSettings` (Singleton).
- Herencia `Weapon` con `ElectricGuitar` y `Drumsticks` (Template Method), y creación de oleadas con `WaveBuilder` (Builder).

## Dos distribuciones, un mismo código

| Edición | Herramientas para preparar | Entregable | Destinatario |
|---|---|---|---|
| Académica | **Oracle JDK 11**, Gradle 7.6.4 y Eclipse | Proyecto fuente, recursos, documentación e informe | Evaluación según rúbrica GM |
| Portable Windows | JDK 17 en Windows y `jpackage` | `distribucion/Deadbeat-Windows-Portable.zip` | Jugadores sin Java instalado |

Los módulos `core` y `lwjgl3` compilan con `options.release = 11`. La construcción de la edición portable se realiza en JDK 17 para incluir un runtime reducido dentro de `Deadbeat.exe`. **No hay dos versiones distintas del código Java.**

## Abrir y ejecutar la edición académica

1. Instalar **Oracle JDK 11** para la comprobación literal de la rúbrica. Eclipse es el IDE de entrega; en desarrollo también puede comprobarse con otra distribución de JDK 11.
2. Situarse en `DeadBeat/`, establecer `JAVA_HOME` a la carpeta raíz del JDK 11 y ejecutar:

   ```powershell
   .\gradlew.bat --version
   .\gradlew.bat clean :core:test :lwjgl3:classes --no-daemon
   .\gradlew.bat :lwjgl3:run
   ```

3. Para preparar Eclipse, ejecutar `Preparar-Eclipse.bat` o `scripts/preparar-eclipse.ps1` conforme a [`docs/INSTALACION_ACADEMICA.md`](docs/INSTALACION_ACADEMICA.md).
4. Importar los proyectos generados `Deadbeat`, `Deadbeat-core` y `Deadbeat-lwjgl3`, y utilizar `Deadbeat-Eclipse.launch` con el directorio de recursos definido.

**Importante:** el workflow académico automatizado utiliza Temurin 11. La prueba final con Oracle JDK 11 debe efectuarse en el equipo del grupo.

## Generar la edición portable

En Windows, con **JDK 17** completo (incluido `jpackage`), ejecutar `Crear-portable-Windows.bat` dentro de `DeadBeat/`. También puede generarse desde **GitHub Actions → Deadbeat - Windows portable → Run workflow**. La carpeta comprimida incluye `Deadbeat.exe`, `runtime/` y `app/` y debe extraerse completa.

Ver [`docs/PORTABLE_WINDOWS.md`](docs/PORTABLE_WINDOWS.md), [`docs/FASE3_VALIDACION_PORTABLE.md`](docs/FASE3_VALIDACION_PORTABLE.md) y [`docs/FASE4_CI_DOBLE.md`](docs/FASE4_CI_DOBLE.md).

## Controles

| Acción | Control |
|---|---|
| Caminar | WASD o flechas |
| Pausa / reanudar | ESC |
| Reiniciar | R |
| Seleccionar una mejora | 1, 2, 3 o clic |
| Pista musical anterior / siguiente | Q / E |
| Recibir daño de prueba | H (depuración) |
| Menú y resultados | Teclado o ratón según la pantalla |

## Documentación para la rúbrica

- **Informe actualizado (GM-1 a GM-10):** [`docs/REPORTE_EJECUTIVO_GM_ACTUALIZADO.pdf`](docs/REPORTE_EJECUTIVO_GM_ACTUALIZADO.pdf) y versiones [DOCX](docs/REPORTE_EJECUTIVO_GM_ACTUALIZADO.docx) / [Markdown](docs/REPORTE_EJECUTIVO_GM_ACTUALIZADO.md).
- **Diagramas de clases y patrones:** `docs/figuras/` y fuentes editables `docs/uml_fuentes/`.
- **Guía de la Fase 5:** [`docs/FASE5_DOCUMENTACION.md`](docs/FASE5_DOCUMENTACION.md).
- **Anexos históricos conservados en la raíz del repositorio:** `Anexo_A_GM3_UML_Completo.pdf`, `Anexo_B_GM6_Singleton.pdf.pdf` y `REPORTE EJECUTIVO DE CUMPLIMIENTO.md`. Verificar concordancia antes de unirlos con el informe actual.

## Historial técnico

Las mejoras del escenario de las versiones 0.4–0.5.3 se explican en `docs/WORLD_0_4.md`, `docs/WORLD_0_5.md`, `docs/WORLD_0_5_1_FIXES.md`, `docs/WORLD_0_5_2_TEXTURE_FIX.md` y `docs/PROPS_0_5_3.md`. Estos documentos describen iteraciones anteriores y no necesariamente el estado completo de la rama `main`.

## Aviso de distribución

El repositorio incorpora música de terceros. Deben comprobarse los derechos de las pistas o sustituirlas por recursos autorizados antes de distribuir públicamente el juego portable.
