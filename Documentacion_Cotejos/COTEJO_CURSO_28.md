# Cotejo 28 — Trabajo en segundo plano con WorkManager

**Lección:** «Trabajo en segundo plano con WorkManager» (*Blur-O-Matic*) del curso Android Basics with Compose.

## Diagnóstico del Cotejo 27

- WorkManager, `CoroutineWorker`, `OneTimeWorkRequest` y cadenas de trabajo: **no existían**.
- Corrutinas, Room, repositorios, `AppContainer`, StateFlow y navegación Compose: **ya existían**.
- Caso de uso real de sincronización con una farmacia: **todavía no existe**. Se aplica a un laboratorio de datos ficticios.

## Implementación añadida

1. `data/background/InventoryReportSummary.kt`: modelo y reglas de conteo/valor del laboratorio Room.
2. `data/background/InventoryReportWorkers.kt`: `PrepareInventoryReportWorker` y `SaveInventoryReportWorker` (`CoroutineWorker`).
3. `data/background/InventoryReportRepository.kt`: `OneTimeWorkRequestBuilder`, datos de entrada/salida, encadenamiento `beginUniqueWork().then().enqueue()`, estados mediante `WorkInfo` y cancelación.
4. `data/background/InventoryReportViewModel.kt`: expone `StateFlow` con separación UI/datos.
5. `ui/background/InventoryReportScreen.kt`: pantalla Compose, título y estados accesibles, botones generar/cancelar y dos `@Preview`.
6. `data/AppContainer.kt`, `ui/home/HomeScreen.kt`, `ui/navigation/FarmAppNavHost.kt` y `FarmAppDestinations.kt`: DI y ruta protegida.
7. `strings.xml`: textos españoles externalizados.

## Correspondencia directa con el codelab

| Codelab | Evidencia de FarmApp |
|---|---|
| Agregar WorkManager al proyecto | `CONFIGURACION_GRADLE_COTEJO_28.md` |
| `CoroutineWorker` y `doWork()` | `PrepareInventoryReportWorker`, `SaveInventoryReportWorker` |
| `OneTimeWorkRequest` | `OneTimeWorkRequestBuilder` para ambas etapas |
| Parámetros de entrada | `SOURCE` para preparar, output de resumen para guardar |
| Datos de salida | conteos/agregados y nombre de archivo |
| Encadenar trabajo | `beginUniqueWork(...).then(save).enqueue()` |
| Mostrar estado | `getWorkInfosForUniqueWorkFlow()`, `StateFlow`, Compose |
| Cancelación | `cancelUniqueWork()` |
| Pruebas | lógica pura, ViewModel falso, interfaz Compose, Worker de guardado |

## Particularidades y limitaciones

- Solo lee `articulos_room_demo` (base local del Cotejo 25); **no es inventario real**.
- La tarea es diferible/persistente; WorkManager no garantiza comenzar inmediatamente ni ejecutarse a una hora exacta.
- El archivo `.txt` se guarda exclusivamente en `files/reportes_demo/` del almacenamiento interno de Android. No se comparte y no requiere almacenamiento externo.
- `WorkManager.Data` solo transporta agregados pequeños, no archivos ni listas.
- No se ha probado la supervivencia ante muerte del proceso, las restricciones de energía ni la compilación integral Android.
- La función de salida genera informes con valores monetarios de *demostración*; no registrar datos clínicos.

## Pruebas añadidas

- `InventoryReportSummaryTest`: cinco casos de éxito/límite.
- `InventoryReportViewModelTest`: tres casos con repositorio falso.
- `InventoryReportScreenTest`: tres pruebas instrumentadas UI.
- `SaveInventoryReportWorkerTest`: una prueba instrumentada de escritura real.

## Estado

Se inspeccionaron los fuentes, XML y recursos, y se ejecutaron comprobaciones de lógica Kotlin aislada. La ejecución del Worker real con la biblioteca AndroidX WorkManager y los tests instrumentados queda pendiente de Gradle/emulador. Es un paquete de **fuentes para integrar**, no un proyecto Gradle completo.
