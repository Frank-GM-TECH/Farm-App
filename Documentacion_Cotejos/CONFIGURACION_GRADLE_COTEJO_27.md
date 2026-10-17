# Gradle — Cotejo 27, Preferences DataStore

En `app/build.gradle.kts` (junto a las demás dependencias de los cotejos 01-26):

```kotlin
dependencies {
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    // Ya existentes: lifecycle-viewmodel-ktx, lifecycle-runtime-compose,
    // kotlinx-coroutines-android, Compose Material3, Navigation, Firebase, Room, etc.
}
```

`1.2.1` es la versión **estable** de DataStore al momento del cotejo (octubre de 2026).
No se requiere plugin KSP nuevo para Preferences DataStore; conserva el KSP que usa Room.
No agregues el delegado `preferencesDataStore` en múltiples lugares: ya existe UNA declaración
a nivel de archivo en `DataStoreUserPreferencesRepository.kt`.

Se requiere `google()` en `settings.gradle.kts` y acceso a los repositorios de dependencias.

Pruebas una vez integrado el proyecto con Gradle:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

El ZIP es un paquete **fuente** para incorporar a Android Studio, NO un proyecto Gradle completo.
La compatibilidad exacta de tu BOM y Kotlin se confirma solamente al compilar en tu instalación.
