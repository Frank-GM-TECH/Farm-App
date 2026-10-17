# Configuración Gradle — Cotejo 28 (WorkManager)

Conserva las dependencias de los 27 cotejos previos. En `app/build.gradle.kts` agrega dentro de `dependencies`:

```kotlin
val workVersion = "2.11.2"
implementation("androidx.work:work-runtime-ktx:$workVersion")
androidTestImplementation("androidx.work:work-testing:$workVersion")
```

WorkManager 2.11.2 es versión estable de marzo de 2026. Se requiere configurar repositorios `google()` y `mavenCentral()` y que el proyecto disponga de un SDK/Kotlin/AGP compatibles.

**No añadas una segunda inicialización manual de WorkManager:** deja la inicialización automática estándar de AndroidX Startup que aporta WorkManager. La clase `FarmAppApplication` sigue creando el `AppContainer` como antes.

Windows PowerShell:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

Si usas un catálogo de versiones `libs.versions.toml`, puedes declarar las dos bibliotecas allí con la misma versión.
