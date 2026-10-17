# Configuración Gradle para Room (Cotejo 25)

El curso oficial utiliza Room y KSP. Consulta [Persist data with Room](https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room?hl=es-419) y las [notas de versiones Room](https://developer.android.com/jetpack/androidx/releases/room).

## 1) Plugin de KSP

Configura **KSP compatible con la versión exacta de Kotlin de tu proyecto**. Si tu `build.gradle.kts` raíz ya declara KSP, no lo dupliques. En el módulo `app/build.gradle.kts` incluye:

```kotlin
plugins {
    id("com.google.devtools.ksp")
}
```

La versión real del plugin debe declararse en el Gradle raíz o en el catálogo de versiones (`libs.versions.toml`) y coincidir con Kotlin. No se fija una versión de KSP a ciegas porque aún no disponemos del Gradle original del proyecto.

## 2) Dependencias Room

En el módulo `app/build.gradle.kts`, dentro de `dependencies`:

```kotlin
val roomVersion = "2.8.5"
implementation("androidx.room:room-runtime:$roomVersion")
ksp("androidx.room:room-compiler:$roomVersion")
implementation("androidx.room:room-ktx:$roomVersion")
androidTestImplementation("androidx.room:room-testing:$roomVersion")
```

Room 2.8.5 es la versión estable publicada en septiembre de 2026. Requiere `minSdk` 23 o superior; FarmApp usa API 26.

Kotlin coroutines, `lifecycle-viewmodel-ktx`, `lifecycle-runtime-compose`, `navigation-compose`, Firebase, Retrofit, Coil y las dependencias de pruebas JUnit/Compose **siguen siendo necesarias** por los cotejos anteriores. No reemplaces ni borres esas dependencias.

## 3) Esquema y migraciones

Se usa `@Database(..., version = 1, exportSchema = false)` **solo en el laboratorio** para simplificar la instalación. En producción conviene activar `exportSchema = true`, configurar `room.schemaLocation` y crear migraciones versionadas. No usar `fallbackToDestructiveMigration()` para datos de usuarios.

## 4) Compilación y pruebas

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

Si el procesador KSP reporta incompatibilidad con Kotlin, comprueba las versiones realmente instaladas de ambos plugins antes de modificarlas.
