# Guía de ejecución del Cotejo 15 — Estrategia de pruebas

1. Crea o abre tu proyecto **FarmApp** Android Studio (Kotlin, Compose, paquete `com.example.farmapp`).
2. Fusiona `app/src` del ZIP acumulativo con el proyecto local, sin borrar recursos propios ni tu configuración Firebase.
3. Agrega dependencias en `app/build.gradle.kts` conforme a `CONFIGURACION_GRADLE_PRUEBAS_15.md` (no copies versiones incompatibles con tu proyecto).
4. Sincroniza Gradle.
5. Verifica que Android Studio reconozca clases bajo `app/src/test/java` como **tests locales**, no instrumentados.
6. Ejecuta `AuthViewModelStrategyTest` y `CatalogViewModelStrategyTest` con el botón Run sobre cada clase.
7. Ejecuta la suite completa local: `./gradlew testDebugUnitTest` (`.\gradlew.bat` en PowerShell).
8. Si todo pasa, habilita cobertura y ejecuta `./gradlew :app:createDebugUnitTestCoverageReport`.
9. Abre el HTML generado y anota porcentaje de cobertura de `AuthViewModel` y `CatalogViewModel`, verificando rutas de error y límites.
10. Para tests Compose previos (ubicados en `src/androidTest`), inicia emulador y ejecuta `connectedDebugAndroidTest`.

## Guion breve para la profesora

> “Separé las pruebas locales de las instrumentadas. Para probar los ViewModels uso repositorios falsos, sin contactar Firebase. Clasifiqué los escenarios en rutas de éxito, de error y casos límite. Verifico `StateFlow` con aserciones de estado y con un colector controlado mediante `backgroundScope`. Cada prueba crea nuevas dependencias para evitar efectos entre tests. La cobertura se medirá con las herramientas de Gradle y JaCoCo después de ejecutar la suite real en Android Studio.”

### Evidencia a conservar

- Captura del árbol `src/test` y `src/androidTest`.
- Captura de tests verdes de JUnit y Compose.
- Reporte HTML de cobertura con fecha, rama o versión de FarmApp y porcentaje de líneas/ramas.
- Cualquier fallo se marca **pendiente** hasta corregirse y reejecutarse.
