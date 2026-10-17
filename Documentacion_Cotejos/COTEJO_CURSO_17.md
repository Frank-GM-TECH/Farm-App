# Cotejo N.º 17 — Diseño adaptable con navegación dinámica

**Curso:** Android Basics with Compose · Codelab «Compila una app adaptable con navegación dinámica» (Reply).

Fuente oficial: https://developer.android.com/codelabs/basic-android-kotlin-compose-adaptive-navigation-for-large-screens?hl=es

## Diagnóstico de la versión 16

Navigation Compose (`NavHost`, `NavController`, rutas y Atrás) **ya estaba presente**. No existía detección de ancho de ventana ni cambio entre barra inferior, riel lateral y panel permanente.

## Matriz de cotejo

| Concepto | Estado C16 | Estado C17 | Evidencia |
|---|---|---|---|
| `WindowWidthSizeClass` | Falta | Incorporado | `MainActivity.kt`, `FarmAppRoot.kt` |
| `calculateWindowSizeClass()` | Falta | Incorporado | `MainActivity.kt` |
| `when` compact/medium/expanded | Falta | Incorporado | `AdaptiveNavigationMode.kt`, `FarmAppAdaptiveNavigation.kt` |
| `NavigationBar` en celular | Falta | Incorporado | `FarmAppAdaptiveNavigation.kt` |
| `NavigationRail` en ancho mediano | Falta | Incorporado | `FarmAppAdaptiveNavigation.kt` |
| `PermanentNavigationDrawer` expandido | Falta | Incorporado | `FarmAppAdaptiveNavigation.kt` |
| Conservar NavHost previo | Sí | Conservado | `FarmAppNavHost.kt` |
| Vistas previas compacta, mediana y expandida | Falta | Incorporadas | `FarmAppAdaptiveNavigation.kt` |
| Pruebas tamaño/navegación | Falta | Añadidas; no ejecutadas en Android | `AdaptiveNavigationTypeTest.kt`, `FarmAppAdaptiveNavigationTest.kt` |

## Decisión de diseño

- **Compact** (ancho menor que 600 dp): barra inferior Inicio / Catálogo.
- **Medium** (de 600 a menos de 840 dp): riel lateral.
- **Expanded** (840 dp o más): panel lateral permanente.
- Autenticación no tiene navegación de destinos privados.
- El detalle del producto mantiene el destino superior **Catálogo** seleccionado.
- El ancho se calcula dentro de Compose; al redimensionar ventana recomponen los controles sin alterar ViewModels o rutas.
- En tablets, Inicio limita la lectura a una columna central con ancho máximo para evitar líneas excesivas.
- El catálogo sigue usando una única lista `LazyColumn`; no se añadió scroll vertical anidado.

## Dependencia Gradle

Agregar dentro de `dependencies {}` en `app/build.gradle.kts`:

```kotlin
implementation("androidx.compose.material3:material3-window-size-class")
```

Mantener el Compose BOM y las dependencias `navigation-compose` del cotejo 16; no se creó un segundo NavController.

## Pruebas

- `src/test/.../AdaptiveNavigationTypeTest.kt`: tres métodos JUnit para comprobar el tipo elegido.
- `src/androidTest/.../FarmAppAdaptiveNavigationTest.kt`: barra, riel, panel y evento Catálogo.
- Continúan las pruebas del Cotejo 16, aún por ejecutar en dispositivo.

**Estado real:** archivos y recursos integrados, XML y ZIP revisados. Se necesita ejecutar `./gradlew :app:assembleDebug`, pruebas JVM/Compose y revisar el modo de ventana redimensionable en Android Studio. Este paquete sigue siendo un conjunto de fuentes, no un proyecto Gradle completo.
