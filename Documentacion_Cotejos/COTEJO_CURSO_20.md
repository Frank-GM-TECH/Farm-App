# Cotejo N.º 20 — Introducción a las corrutinas en Android Studio

**Curso:** Android Basics with Compose; codelab «Introducción a las corrutinas en Android Studio» (Race Tracker).

## Comparación con Cotejo 19

| Requisito del curso | C19 | C20 y evidencia |
|---|---|---|
| `suspend`, `delay`, `CoroutineScope` / Job | Existían | Conservados, aplicados a progreso por pasos |
| Concurrencia estructurada | Existía | `DualCatalogSyncRunner`: dos `launch` hijos en `coroutineScope` |
| Dos barras de progreso independientes | Faltaban | `CatalogSyncProgressPanel.kt` |
| Botones Iniciar/Pausar/Reanudar/Reiniciar | Faltaban | `CatalogSyncViewModel.kt` + Panel |
| Cancelación sin perder progreso de pausa | Faltaba | `CatalogSyncViewModel.pause()` / `startOrResume()` |
| Reinicio a cero | Faltaba | `CatalogSyncViewModel.reset()` |
| Pruebas de progreso con tiempo virtual | Parcial | `CatalogSyncProgressTest.kt`: 7 pruebas con `runTest`, `advanceTimeBy`, `runCurrent`, `advanceUntilIdle` |
| Pruebas Compose para controles | Parcial | `CatalogSyncProgressPanelTest.kt`: 3 pruebas |

## Funcionalidad real en el proyecto

En el encabezado del catálogo (compacto o de dos paneles) se agregó **Progreso de sincronización (demo)** junto a la consulta asíncrona del Cotejo 19. Dos tareas ficticias avanzan en 10% por paso con diferentes pausas suspendibles:

- Productos: un paso cada 200 ms simulados.
- Existencias: un paso cada 100 ms simulados.

No actualiza Firestore ni otros inventarios. Permite probar corrutinas sin usar datos privados ni servicios reales.

## Arquitectura

- `catalog/CatalogSyncProgress.kt`: `SyncProgressTask`, `SteppedSyncProgressTask.run()` (`suspend`, `delay`), `DualCatalogSyncRunner` (corrutinas hijas concurrentes).
- `catalog/CatalogSyncViewModel.kt`: estados `IDLE/RUNNING/PAUSED/FINISHED/ERROR`, `StateFlow`, `viewModelScope.launch`, `Job.cancel`, reinicio y reanudación.
- `ui/catalog/CatalogSyncProgressPanel.kt`: interfaz Compose sin estado de negocio; dos `LinearProgressIndicator`, botones y semántica accesible.
- `ui/navigation/CatalogRouteScreen.kt`: integración con `collectAsStateWithLifecycle` y callbacks; sin alteración de navegación, filtros o autenticación.
- `values/strings.xml`: textos traducibles.
- `test/.../CatalogSyncProgressTest.kt`: siete pruebas locales JUnit.
- `androidTest/.../CatalogSyncProgressPanelTest.kt`: tres pruebas instrumentadas Compose.

## Qué explicar a la profesora

Como en *Race Tracker*, las corrutinas permiten que dos progresos avancen sin bloquear el hilo principal. `launch` crea trabajos hijos, `coroutineScope` los espera, `delay()` suspende la ejecución y `Job.cancel()` cancela cooperativamente. El ViewModel mantiene el progreso al pausar, y lo reinicia cuando lo solicita el usuario. `runTest` y `advanceTimeBy` permiten probar el comportamiento usando tiempo virtual, sin esperar los segundos reales del ejercicio.

## Estado de validación

- **Revisión estática:** integridad del ZIP, XML, referencias de strings, conservación de fuentes y conexión entre ViewModel y UI.
- **Prueba aislada JVM de la lógica:** se compila la parte de corrutinas y se ejecuta una comprobación no-JUnit (ver `VALIDACION_20.txt`).
- **Pendientes en Android Studio:** compilación completa Gradle, siete pruebas unitarias nuevas reales, tres instrumentadas y comprobación manual de pausa/resume/rotación.

Este es un ZIP acumulativo de código fuente; **no** es un proyecto Android Studio/Gradle autónomo.
