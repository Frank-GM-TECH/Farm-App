# Cotejo 29 — Pruebas y WorkManager avanzados

**Lección:** «Pruebas y WorkManager avanzados» (Blur-O-Matic), Android Basics with Compose.

## Diagnóstico de versión 28

**Ya existían**:
- Cadena única con `beginUniqueWork`, nombre `farmapp_room_report_demo`, política `REPLACE` y dos etiquetas de etapa.
- UI reactiva basada en `WorkInfo` (espera, ejecución, éxito, fallo, cancelado), botón cancelar y archivo final.
- `CoroutineWorker`, `Data` de entrada/salida y dos Workers encadenados.
- Pruebas de modelo, repositorio falso, pantalla y guardado con `TestListenableWorkerBuilder`.

**Faltaba completar o mejorar**:
- Restricciones de ejecución de WorkRequest.
- Evitar reemplazar un trabajo aún pendiente ante una solicitud accidental repetida.
- Etiqueta común para todas las etapas y selección de la ejecución más reciente sin suponer orden de `WorkInfo`.
- Pruebas del plan de trabajo y casos de fallo de ambas etapas.
- Ejemplo del `TestDriver` para simular restricciones.

## Implementación de la versión 29

1. `InventoryReportWorkPlan.create()` construye el trabajo Preparar y Guardar, con etiqueta común, etiquetas por etapa y un token de ejecución compartido.
2. El primer trabajo requiere **batería no baja**; el segundo requiere **almacenamiento no bajo**. Como ambos trabajan con la base local, no se exige Internet ni carga del dispositivo.
3. La política de trabajo único ahora es `ExistingWorkPolicy.KEEP`, para no descartar accidentalmente una cadena todavía pendiente o ejecutándose. Tras concluir, la aplicación puede pedir una nueva.
4. `readReportStatus()` separa ejecuciones anteriores por token y muestra **En espera** si el siguiente Worker aún no arranca. La app no afirma conocer con precisión si espera debido a una restricción o al planificador.
5. `InventoryReportScreen.kt` muestra las restricciones y sigue ofreciendo Cancelar e información de archivo al completar.
6. Pruebas Kotlin puras de selección de token, pruebas Android de petición/etiquetas/restricciones, workers con `TestListenableWorkerBuilder`, ejemplo de `WorkManagerTestInitHelper` y `TestDriver`, más pruebas Compose.

## Matriz académica

| Tema codelab | Implementación FarmApp |
|---|---|
| Trabajo único | `UNIQUE_NAME` y `ExistingWorkPolicy.KEEP` en InventoryReportRepository.kt |
| Etiquetas | `TAG_REPORT`, `TAG_PREPARE`, `TAG_SAVE` y `RUN_TAG_PREFIX` |
| Estados UI | `getWorkInfosForUniqueWorkFlow`, `readReportStatus`, `StateFlow`, Compose |
| Resultado final | `ReportWorkKeys.FILE_NAME`, mensaje en UI |
| Cancelar | `cancelUniqueWork` desde UI |
| Restricciones | `setRequiresBatteryNotLow`, `setRequiresStorageNotLow` |
| API de pruebas | `work-testing`, `WorkManagerTestInitHelper`, `TestDriver`, `TestListenableWorkerBuilder` |
| Inspector | Ejecutar Android Studio → App Inspection → Background Task Inspector |

## Precauciones

- Los archivos `.txt` son educativos y privados del dispositivo; no son un respaldo real de farmacia.
- `WorkManager` no garantiza ejecución inmediata. No usarlo para un cronómetro exacto.
- Los tests de integración con WorkManager comparten un singleton en el proceso; ejecutarlos de manera aislada si otras pruebas inician WorkManager de producción.
- Se mantiene el mismo identificador de trabajo único y la misma fuente Room; no se crea una base nueva.
- Se mantienen todos los cotejos anteriores. El paquete **no** incluye proyecto Gradle completo.
- Sin Android SDK en el entorno, la compilación Android real y el Inspector deben validarse en Android Studio.
