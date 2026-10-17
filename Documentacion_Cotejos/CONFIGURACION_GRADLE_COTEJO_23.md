# Gradle — Imágenes de Internet con Coil

Este ZIP no incluye archivos de configuración de Gradle para evitar sobrescribir el proyecto real. Agrega al módulo `app/build.gradle.kts` existente:

```kotlin
dependencies {
    // Coil 2.x, la API coinicidente con import coil.compose.AsyncImage
    implementation("io.coil-kt:coil-compose:2.7.0")
}
```

Debes conservar las dependencias de los cotejos anteriores: Retrofit, convertidores, plugin y runtime de kotlinx.serialization, Navigation Compose, Lifecycle ViewModel/Compose, Material 3 y Firebase Auth. Verifica que todas las versiones sean compatibles con la versión real del proyecto.

No es necesario volver a añadir `INTERNET`, ya está en `AndroidManifest.xml`. El código de Coil 3.x utiliza otras coordenadas/imports: no combines una dependencia 3.x con `coil.compose.AsyncImage` de Coil 2.x.
