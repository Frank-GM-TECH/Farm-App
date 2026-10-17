# Cotejo 29 — Guía práctica en Android Studio

1. Integra los archivos del ZIP sobre tu proyecto Compose actual; conserva la configuración Gradle de los cotejos 1–28.
2. Comprueba en `app/build.gradle.kts` la dependencia `work-runtime-ktx` y, para las pruebas instrumentadas, `work-testing` (ver CONFIGURACION_GRADLE_COTEJO_28.md).
3. Inicia el emulador. Entra al laboratorio Room, agrega algunos artículos ficticios y vuelve a Inicio.
4. Abre «Generar informe en segundo plano (demo)». Lee las restricciones explicadas debajo de los botones.
5. Presiona «Generar» dos veces. La política KEEP debe mantener solo una cadena activa si la primera aún no termina. WorkManager puede completarla rápidamente; si está terminada, la siguiente solicitud crea otra.
6. Observa «En espera», «Preparando», «Guardando», y «Completado». Pulsa Cancelar mientras esté pendiente para comprobar el estado CANCELLED.
7. Inspecciona `App Inspection → Background Task Inspector` (la ubicación puede variar con Android Studio): identifica el nombre único `farmapp_room_report_demo` y las etiquetas de los WorkRequests.
8. Ejecuta las pruebas con PowerShell:

   ```powershell
   .\gradlew.bat :app:testDebugUnitTest
   .\gradlew.bat :app:connectedDebugAndroidTest
   ```

9. En Android Studio, ejecuta de forma aislada `AdvancedInventoryReportWorkTest` si las pruebas de otras suites ya inicializaron el singleton de WorkManager; utiliza un emulador limpio si fuese necesario.
10. Abre `AdvancedInventoryReportWorkerTest.kt` y explica `TestListenableWorkerBuilder`, luego `AdvancedInventoryReportWorkTest.kt` para `TestDriver` y restricciones simuladas.

## Cómo justificarlo ante tu profesora

La app reutiliza dos Workers reales de la versión anterior, y ahora aplica condiciones de ejecución, evita reemplazos accidentales, identifica la ejecución actual mediante etiquetas y dispone de una estrategia de pruebas del plan y los Workers. La comprobación en el Inspector debe documentarse con capturas de Android Studio, no con verificaciones aisladas de Kotlin.
