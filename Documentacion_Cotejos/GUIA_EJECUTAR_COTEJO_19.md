# Cotejo 19 — Cómo demostrar corrutinas en Android Studio

1. Abre tu proyecto FarmApp existente e integra el contenido de `app/src` del ZIP.
2. Asegúrate de tener dependencias de Lifecycle ViewModel, coroutines y Compose BOM
   como en los cotejos anteriores.
3. Si no lo tienes, añade en `app/build.gradle.kts` la dependencia para las pruebas locales:

```kotlin
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
```

La versión debe ser compatible con el `kotlinx-coroutines-core` usado por tu proyecto.
4. Sincroniza Gradle. Inicia FarmApp con la cuenta de prueba de Firebase.
5. Ingresa a **Catálogo** y ubica «Consulta asíncrona de inventario (demo)».
6. Presiona **Consultar**: observa el estado de carga, seguido del total ficticio.
7. Presiona **Consultar** otra vez y después **Cancelar** rápidamente: debe aparecer
   «Consulta cancelada por el usuario».
8. Abre `CatalogAvailability.kt` y explica las funciones `suspend`, `coroutineScope`,
   `async`, `awaitAll` y `withContext`.
9. En `CatalogAvailabilityViewModel.kt`, explica `viewModelScope.launch` y `Job.cancel()`.
10. Ejecuta los tests locales:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

11. Inicia un emulador y ejecuta tests de UI:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

12. Guarda capturas del resultado de pruebas y del funcionamiento, para adjuntarlas
    al informe académico.

### Pruebas del curso documentadas

Éxito (34 unidades demo), fallo de fuente, reintento, carga duplicada, cancelación
propagada a hijos y cancelación automática por excepción de un hijo.

### Restricción de la entrega

Se trata de fuentes integrables sobre una plantilla Android Studio con Gradle;
**no** de un proyecto Gradle autónomo listo para ejecutar por sí solo.
