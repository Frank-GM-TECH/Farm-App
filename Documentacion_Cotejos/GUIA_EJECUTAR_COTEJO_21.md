# Guía de Android Studio — Cotejo 21

1. Integra los cambios `app/src` de la versión 21 en tu proyecto Compose `com.example.farmapp`.
2. Añade el plugin de serialización Kotlin en `plugins`, **misma versión que el plugin de Kotlin actual**. En la raíz declara el plugin con `apply false` si todavía no existe; no dupliques versiones.
3. En `app/build.gradle.kts`, agrega:

   ```kotlin
   implementation("com.squareup.retrofit2:retrofit:2.11.0")
   implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
   implementation("com.squareup.retrofit2:converter-scalars:2.11.0")
   implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
   testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2") // si falta
   ```

   Si la plantilla usa Kotlin anterior a 2.2, selecciona una versión de runtime serialization compatible con la versión de Kotlin y revisa la compatibilidad global antes de sincronizar.

4. Confirma que `AndroidManifest.xml` ya tiene `<uses-permission android:name="android.permission.INTERNET"/>`.
5. Sincroniza Gradle y ejecuta FarmApp con un emulador conectado a Internet.
6. Inicia sesión, entra al catálogo y pulsa **Ver JSON** para mostrar el texto sin analizar; luego **Consultar Internet** para obtener objetos Kotlin tipados.
7. Debe cambiar de IDLE a LOADING y luego SUCCESS. Verás nombre y precio en USD de artículos externos ficticios.
8. Activa Modo avión y vuelve a consultar: debe mostrarse un error y permitir reintentar cuando restablezcas Internet.
9. Abre `RemoteProductDto.kt`, explica `@Serializable` y `@SerialName("title")`.
10. Abre `RemoteProductsService.kt`, explica `Retrofit`, `@GET` y `by lazy`.
11. Abre `RemoteProductsViewModel.kt`, explica `StateFlow`, manejo de `IOException`/`HttpException`/`SerializationException` y cancelación.
12. Abre `RemoteProductsPanel.kt`: muestra `@Preview` de éxito y error, sin usar red.

Para pruebas locales: `./gradlew :app:testDebugUnitTest` (Windows: `.\gradlew.bat :app:testDebugUnitTest`).
Para pruebas Android con emulador: `./gradlew :app:connectedDebugAndroidTest`.

**No usar la API ficticia como base de medicamentos, inventario, precios ni recetas reales.**
