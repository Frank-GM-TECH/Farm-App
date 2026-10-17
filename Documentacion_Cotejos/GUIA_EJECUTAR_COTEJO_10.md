# Guía para Android Studio — Cotejo 10

1. Abre tu proyecto `FarmApp` de Android Studio con Kotlin, Jetpack Compose y Material 3.
2. Integra los archivos `app/src/main/java` y `app/src/main/res` del ZIP.
3. Si ya existe un `AndroidManifest.xml`, NO lo reemplaces sin revisar: fusiona su Activity principal,
   permisos e íconos con el manifest incluido (este incluye `MainActivity` y `INTERNET`).
4. Sincroniza Gradle y ejecuta FarmApp en emulador o teléfono.
5. En la parte superior, presiona **Sistema**, **Claro** y **Oscuro**; observa cómo cambian
   fondos, textos, tarjetas, botones y chips.
6. Abre `ui/theme/Theme.kt` para ejecutar las dos vistas previas de tema.
7. Abre `ui/components/ThemeModeSelector.kt` y ejecuta `ThemeModeSelectorPreview()` en *Interactive mode*.
8. Para la prueba de UI, ejecuta `ThemeModeSelectorTest` en `androidTest`.

**Para la profesora:** las paletas en Compose no son iguales al tema XML de arranque.
`MaterialTheme` gobierna el contenido Compose; `styles.xml` proporciona el estilo
de ventana necesario para `MainActivity` y el arranque Android.

**Pendiente:** instalación real, compilación y pruebas en el IDE con dependencias y Firebase configurados.
