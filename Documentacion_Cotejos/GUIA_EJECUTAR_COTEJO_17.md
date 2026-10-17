# Cotejo 17: guía de demostración en Android Studio

1. Copia/combina `app/src` de este paquete con tu proyecto FarmApp existente. No sustituyas el `build.gradle.kts` que generó Android Studio.
2. En `app/build.gradle.kts`, añade `implementation("androidx.compose.material3:material3-window-size-class")` y sincroniza Gradle.
3. Verifica la dependencia de Navigation Compose del Cotejo 16 y la configuración Firebase existente; no uses cuentas reales en clase.
4. Abre `MainActivity.kt` y señala `calculateWindowSizeClass(this@MainActivity)`.
5. Abre `FarmAppAdaptiveNavigation.kt` y explica el `when` de `WindowWidthSizeClass`.
6. Abre el panel **Design > Preview** del mismo archivo. Verifica las tres funciones `FarmAppCompactPreview`, `FarmAppMediumPreview` y `FarmAppExpandedPreview`.
7. Inicia sesión con una cuenta de prueba en un emulador y navega Inicio → Catálogo → Producto → Atrás.
8. Usa un emulador **resizable** o tablet; redimensiona y observa cómo cambian barra/riel/panel sin perder la navegación.
9. En el emulador con tablet, comprueba también el modo horizontal y que los botones se pueden pulsar.
10. Ejecuta `AdaptiveNavigationTypeTest` como prueba JVM y `FarmAppAdaptiveNavigationTest` con dispositivo/emulador para corroborar los tres modos.

Comandos orientativos (Windows):

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

**Importante:** No declaramos estas compilaciones ni pruebas instrumentadas aprobadas hasta ver sus resultados reales.
