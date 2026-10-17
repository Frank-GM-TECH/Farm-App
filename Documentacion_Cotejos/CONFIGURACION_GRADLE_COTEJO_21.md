# Fragmentos Gradle para retrofit + kotlin.serialization (C21)

Este ZIP no incluye Gradle; usa el de la plantilla creada con Android Studio. Agrega solamente lo faltante.

## Plugin Kotlin Serialization

El plugin **debe tener la misma versión que tu Kotlin**. Si `app/build.gradle.kts` usa `alias(libs.plugins.kotlin.android)` con Version Catalog, agrega en `gradle/libs.versions.toml`:

```toml
[plugins]
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

Luego en el `plugins {}` de módulo `app`:

```kotlin
alias(libs.plugins.kotlin.serialization)
```

Alternativa sin catálogo: en Gradle raíz dentro de `plugins {}` agrega:

```kotlin
id("org.jetbrains.kotlin.plugin.serialization") version "VERSION_KOTLIN_QUE_YA_USAS" apply false
```

Y en `app/build.gradle.kts` dentro de `plugins {}`:

```kotlin
id("org.jetbrains.kotlin.plugin.serialization")
```

## Dependencias módulo `app`

```kotlin
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
implementation("com.squareup.retrofit2:converter-scalars:2.11.0")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
```

Esta entrega usa el **conversor oficial de Retrofit**, no el conversor comunitario legado (archivado en 2024). Ajusta la versión de kotlinx.serialization según la versión Kotlin de la plantilla. No se ha compilado Gradle en este entorno.

## Pruebas

```kotlin
testImplementation("junit:junit:4.13.2")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
```

Las dependencias UI se deben gestionar usando el BOM Compose de la plantilla.
