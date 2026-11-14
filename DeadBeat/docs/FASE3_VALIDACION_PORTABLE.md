# Fase 3 - Comprobar la version portable de Deadbeat

## Estado al preparar esta fase

- El repositorio usa Gradle Wrapper **7.6.4**.
- `core` y `lwjgl3` mantienen `options.release = 11`.
- La version portable se construye con **JDK 17**, utilizando `jpackage`.
- Eclipse y el generador de proyectos academicos no necesitan cambiar.
- El ultimo GitHub Actions exitoso observado pertenecia a una version **anterior** a las fases 1 y 2.

## Archivos agregados o modificados

- `.github/workflows/deadbeat-portable-windows.yml`: valida Gradle 7.6.4 + JVM 17, construye el paquete y verifica su contenido antes de subirlo.
- `DeadBeat/scripts/verificar-portable-windows.ps1`: comprueba el ZIP, el runtime Java 17, los JAR de `core` y `lwjgl3` y los sprites/mapas/metadatos obligatorios.
- `DeadBeat/Crear-portable-Windows.bat`: al construir localmente ejecuta la misma verificacion.
- `DeadBeat/.gitignore`: ignora las distribuciones generadas, sin eliminar ninguna del disco.

## Metodo recomendado: GitHub Actions

1. Sube los archivos anteriores a `main` de `Klonoh/Deadbeat`.
2. En GitHub abre **Actions > Deadbeat - Windows portable > Run workflow**.
3. Comprueba que terminan con exito los pasos **Verificar Gradle 7.6.4 ejecutado con JDK 17**, **Generar Deadbeat.exe** y **Verificar ejecutable, Java y recursos empaquetados**.
4. Descarga el artefacto **Deadbeat-Windows-Portable**. GitHub puede entregar un ZIP externo que contiene otro ZIP con el juego. Extrae ambos cuando corresponda.
5. Dentro del ZIP interno extraido, abre `Deadbeat/Deadbeat.exe`.

## Metodo alternativo: Windows con JDK 17

En `DeadBeat/` ejecuta `Crear-portable-Windows.bat` con JDK 17 configurado, o:

```powershell
.\scripts\crear-portable-windows.ps1 -JdkHome 'C:\ruta\a\jdk-17'
.\scripts\verificar-portable-windows.ps1
```

El ZIP final queda en `DeadBeat/distribucion/Deadbeat-Windows-Portable.zip`.

## Prueba manual imprescindible

Usa preferentemente otro PC con Windows x64, **sin Java instalado**:

1. Extrae la carpeta `Deadbeat/` completa. No muevas el `.exe` fuera de esa carpeta.
2. Abre `Deadbeat.exe` y comprueba que se ve el menu.
3. Inicia una partida y verifica sprites, escenario, sonido y entradas de teclado.
4. Comprueba el combate, el avance de oleadas, las mejoras y las pantallas finales.
5. Cierra el juego y comprueba que termina sin procesos bloqueados.

Los scripts solo verifican la **estructura del paquete y la presencia del runtime**; no sustituyen una prueba grafica real.

## Nota sobre la rubrica

Usar JDK 17 **para empaquetar** no cambia el objetivo de compilacion Java 11: `core` y `lwjgl3` siguen teniendo `options.release = 11`. La entrega academica se valida por separado con **Oracle JDK 11** y Eclipse, tal como exige la pauta.

## Licencias

El repositorio incluye canciones de terceros. Revisa y resuelve los derechos de esas pistas antes de distribuir publicamente el ZIP con musica incorporada.
