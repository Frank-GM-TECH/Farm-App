# Cotejo 12 — Guía de pruebas de accesibilidad (Android Studio)

1. Integra el contenido de `app/src` en tu proyecto Compose de FarmApp, conservando la configuración Gradle/Firebase del proyecto original.
2. Revisa `AuthScreen.kt`, `AuthPasswordField.kt`, `CatalogLearningPreview.kt` y `AnimatedProductDemoCard.kt`: busca `heading`, `liveRegion`, `contentDescription`, `stateDescription` y `heightIn(min = 48.dp)`.
3. Con un emulador iniciado, ejecuta `./gradlew :app:connectedDebugAndroidTest` (Windows: `./gradlew.bat :app:connectedDebugAndroidTest`).
4. Abre `FarmAppAccessibilityTest.kt` y revisa las cuatro pruebas con el test runner.
5. Activa TalkBack desde ajustes de accesibilidad del emulador, navega los formularios y los productos, y confirma que las descripciones de estado se entienden.
6. Amplía el tamaño de fuente y de visualización, prueba tema claro y oscuro y revisa los controles táctiles.
7. Registra capturas y resultados **solo después de ejecutar** cada paso. Las pruebas automatizadas no sustituyen las pruebas manuales de accesibilidad.

### Limitaciones

Los archivos del ZIP siguen requiriendo un proyecto Android Studio con Gradle y sus dependencias. No se ha realizado una compilación completa Android en este entorno.
