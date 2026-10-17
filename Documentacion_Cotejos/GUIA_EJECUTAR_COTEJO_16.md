# Guía Cotejo 16 — Navegación en Android Studio

1. Copia **todos los archivos de `app/src`** del nuevo ZIP a tu proyecto FarmApp, fusionándolos con los de Android Studio.
2. En `app/build.gradle.kts`, dentro de `dependencies`, agrega:

   ```kotlin
   implementation("androidx.navigation:navigation-compose:2.10.2")
   androidTestImplementation("androidx.navigation:navigation-testing:2.10.2")
   ```

3. Sincroniza Gradle. Mantén las dependencias de los cotejos previos y Firebase configurado con tus propias credenciales.
4. Abre `ui/navigation/FarmAppDestinations.kt` y muestra las rutas declaradas.
5. Abre `ui/navigation/FarmAppNavHost.kt` y señala `NavHost`, `rememberNavController`, `composable`, `navigate`, `popBackStack`, `navArgument`, `Intent.ACTION_SEND` y `LaunchedEffect`.
6. En emulador, inicia sesión (HU01), abre **Explorar catálogo**, pulsa **Abrir ficha** en cualquier producto, retrocede y vuelve.
7. Pulsa **Compartir ficha educativa** y comprueba que Android muestra aplicaciones para compartir texto.
8. Cierra sesión. Comprueba que Atrás no regresa a catálogo ni a detalle.
9. Ejecuta la nueva clase `FarmAppNavigationTest` desde la carpeta `androidTest` usando un emulador.

### Pruebas

- Unitarias JVM: `./gradlew :app:testDebugUnitTest` (Windows: `./gradlew.bat :app:testDebugUnitTest`).
- Instrumentadas: `./gradlew :app:connectedDebugAndroidTest` (Windows: `./gradlew.bat :app:connectedDebugAndroidTest`).
- El proyecto entregado sigue siendo un **paquete de archivos fuente**, no un proyecto Gradle autónomo. Debes pegarlo en la plantilla Empty Activity creada con Compose.

### Evidencia para tu profesora

Presenta capturas de:
1. `FarmAppDestinations.kt` (rutas y argumento `sku`).
2. `FarmAppNavHost.kt` (`NavHost` y callbacks).
3. La barra superior y navegación Atrás de Catálogo y Detalle.
4. Selector para compartir por Intent.
5. Ventana **Run** de la prueba instrumentada solo después de ejecutarla en tu equipo.
