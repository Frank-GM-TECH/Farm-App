# Guía para demostrar el Cotejo 28 en Android Studio

1. Crea/abre FarmApp como proyecto Compose y fusiona `app/src` del ZIP con tu proyecto.
2. Agrega `work-runtime-ktx` y `work-testing` en `app/build.gradle.kts` según `CONFIGURACION_GRADLE_COTEJO_28.md`; sincroniza Gradle.
3. Configura Firebase y el resto de dependencias de cotejos anteriores y comprueba la compilación, pendiente hasta que se realice en Android Studio.
4. Inicia sesión; abre **Laboratorio Room local (demo)**, crea dos o tres artículos ficticios y vuelve a Inicio.
5. Pulsa **Generar informe en segundo plano (demo)** y **Generar / volver a generar**.
6. Observa el estado: en cola, preparando, guardando, completado. Los trabajos pueden terminar muy rápido; WorkManager NO garantiza horario exacto.
7. Regresa a Inicio, espera y vuelve a abrir la pantalla: WorkManager registra el trabajo y su resultado incluso si deja de mostrarse esta pantalla.
8. Inspecciona con **View > Tool Windows > App Inspection > Background Task Inspector** (si tu versión lo ofrece): busca el trabajo único `farmapp_room_report_demo`.
9. En **Device Explorer** busca `data/data/com.example.farmapp/files/reportes_demo/` y revisa el texto que se generó; puede ser necesario un emulador depurable.
10. Repite la tarea o cancélala mientras esté en espera. Una tarea muy breve puede finalizar antes de recibir la orden de cancelar: eso es normal.
11. Ejecuta pruebas locales e instrumentadas.

## Para tu profesora

Abre `InventoryReportWorkers.kt` (dos `CoroutineWorker`), `InventoryReportRepository.kt` (`OneTimeWorkRequestBuilder`, `.then()`, `workDataOf`, `WorkInfo`), `InventoryReportViewModel.kt` (`StateFlow`) y `InventoryReportScreen.kt` (Compose). Explica que es una tarea de almacenamiento local diferible, no un servicio de tiempo real, y que los artículos son solo ejemplos.

## Precauciones

- La base SQLite directa del Cotejo 24 y Room del Cotejo 25 permanecen independientes.
- No copies rutas internas de dispositivo a la web ni uses información real de pacientes.
- No se programa trabajo periódico ni una alerta real de farmacia; WorkManager se usará para esa etapa cuando se implemente HU06.
