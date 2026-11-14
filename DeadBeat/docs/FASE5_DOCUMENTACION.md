# Fase 5 — Documentación académica de Deadbeat

**Repositorio contrastado:** https://github.com/Klonoh/Deadbeat

**Commit utilizado:** `a2f7d4b1fddcd0d48232e6750be37c6d63d85b02`.

**Estado observado:** [JDK 11 aprobado](https://github.com/Klonoh/Deadbeat/actions/runs/43540090728); [portable Windows aprobado](https://github.com/Klonoh/Deadbeat/actions/runs/43540090732).

## Qué incluye este parche

- `docs/REPORTE_EJECUTIVO_GM_ACTUALIZADO.md`: reporte revisado por GM-1 a GM-10.
- `docs/REPORTE_EJECUTIVO_GM_ACTUALIZADO.docx` y `docs/REPORTE_EJECUTIVO_GM_ACTUALIZADO.pdf`: versiones para revisar y entregar.
- `README.md` de `DeadBeat/`: índice actualizado de ejecución, entregas y documentación (sustituye el README antiguo dedicado a la versión 0.5.3).
- `docs/figuras/`: diagramas UML PNG/SVG/PDF y tres maquetas de la interfaz.
- `docs/uml_fuentes/`: archivos DOT de todos los diagramas y fuentes PlantUML de patrones.

## Respeto por los archivos existentes

**No se reemplazan** `REPORTE EJECUTIVO DE CUMPLIMIENTO.md`, `Anexo_A_GM3_UML_Completo.pdf` ni `Anexo_B_GM6_Singleton.pdf.pdf` de la raíz. No se modifica ninguna clase `.java`, ningún recurso, Gradle, Eclipse, workflow ni script portable. **Sí se actualiza `DeadBeat/README.md`** para quitar instrucciones obsoletas, conservando los registros históricos en `docs/WORLD_*.md` y `docs/PROPS_0_5_3.md`.

El informe nuevo está escrito para el estado de la rama `main` en el commit señalado. El informe previo incluye pendientes ya resueltos, alude a `GameSession` (no implementada) y contiene referencias a `DEADBEAT_RUTA_DESARROLLO.md` (eliminada del repositorio). **La implementación real del Singleton es `AudioSettings`.**

## Cómo usarlo

1. Extraer el ZIP **en la raíz exterior del repositorio**, donde ya están `.github/`, `DeadBeat/` y los anexos PDF.
2. Revisar `DeadBeat/docs/REPORTE_EJECUTIVO_GM_ACTUALIZADO.pdf` y las figuras UML.
3. Si los anexos PDF previos difieren del código actual, usar los nuevos diagramas o reemplazar esos anexos al preparar la entrega; no adjuntar dos versiones contradictorias.
4. Cambiar maquetas por capturas reales si el equipo dispone de ellas; la rúbrica admite maquetas.
5. Verificar el proyecto con **Oracle JDK 11 y Eclipse**, además de los flujos CI.
6. Preparar en Fase 6 las dos distribuciones independientes sin incluir el mismo juego duplicado en el informe.

## Limitaciones de la revisión

El contenido interno de los dos PDF históricos del repositorio no estuvo disponible para cotejo visual directo; se conservaron para evitar pérdidas. Los diagramas nuevos se reconstruyeron desde el código fuente actual. Los números de línea del informe corresponden al commit `a2f7d4b` y pueden cambiar si el repositorio evoluciona.

Los workflows verdes prueban construcción y pruebas automatizadas; no prueban por sí solos la apertura gráfica del juego en otro PC ni la distribución exacta Oracle de la JVM 11.

## Commit sugerido

`docs: actualizar informe GM1-GM10 y anexos UML desde código actual`
