# Guía — Cotejo 20, corrutinas en Android Studio

1. En un proyecto Android Studio Compose que ya tenga integrados los cotejos 01–19, incorpora `app/src` de este ZIP. Fusiona recursos XML y Manifest; no sobrescribas configuraciones personalizadas.
2. Mantén las dependencias previas de ViewModel, Lifecycle, Compose y Kotlin coroutines. Para las pruebas locales añade en `app/build.gradle.kts`:
   ```kotlin
   testImplementation("junit:junit:4.13.2")
   testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
   ```
   Las versiones deben ser coherentes con las del proyecto real y del Compose BOM.
3. Ejecuta `./gradlew :app:testDebugUnitTest` o `gradlew.bat :app:testDebugUnitTest` en Windows.
4. Inicia un emulador y ejecuta `./gradlew :app:connectedDebugAndroidTest` para las pruebas Compose.
5. Inicia sesión y abre Catálogo; busca la tarjeta **Progreso de sincronización (demo)** debajo de la consulta asíncrona del Cotejo 19.
6. Presiona **Iniciar** y comprueba que ambas barras avanzan sin congelar botones ni filtros. Existencias terminará antes que Productos.
7. Presiona **Pausar**, espera y comprueba que no avanza. Selecciona **Reanudar** y observa que continúa desde el progreso anterior.
8. Presiona **Reiniciar** y verifica que las dos barras vuelven a 0.
9. Abre las previews `CatalogSyncPausedPreview` y `CatalogSyncFinishedPreview` sin depender de Firebase.
10. Enseña a tu profesora `DualCatalogSyncRunner`, `CatalogSyncViewModel`, `CatalogSyncProgressPanel` y `CatalogSyncProgressTest`.

## Evidencia sugerida

Capturas de Android Studio (preview y dispositivo), ejecución de test local JUnit con verde, prueba instrumentada y breve vídeo de pausa/reanudación. No declares aprobadas esas pruebas hasta ejecutarlas realmente.
