# GUÍA COTEJO 14 — ViewModel y estado de la UI en FarmApp

## Integración

1. Crea o abre el proyecto `FarmApp` (Empty Activity / Kotlin / Jetpack Compose, paquete `com.example.farmapp`).
2. Fusiona esta versión de `app/src` sobre la versión anterior para no borrar archivos propios del proyecto.
3. Mantén las dependencias del `README.md`: `lifecycle-viewmodel-compose`, `lifecycle-runtime-compose`, Material 3 y Compose BOM. `SavedStateHandle` y la creación de ViewModels necesitan dependencias Lifecycle coherentes.
4. Sincroniza Gradle.
5. Si aún no conectaste Firebase, puedes abrir `CatalogLearningPreviewPreview()` sin iniciar sesión; la vista previa interactiva no usa Firebase. Para comprobar eventos y rotación dentro de la app, configura Firebase y entra al catálogo educativo, o crea una pantalla temporal que muestre `CatalogLearningPreview()`.

## Explicación paso a paso

- **CatalogUiState**: reúne consulta, filtro y resultados como un `data class`.
- **CatalogViewModel**: maneja los cambios mediante `onQueryChanged()` y `onCategorySelected()`, sin depender de Compose ni Firebase.
- **CatalogLearningPreview**: ruta que recibe un ViewModel y recoge su estado con `collectAsStateWithLifecycle()`.
- **CatalogScreenContent**: composable sin estado de negocio, personalizable y fácil de probar.

## Pruebas

Pruebas locales:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Pruebas de interfaz (emulador/device activo):

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

Para comprobar rotación: busca `gasas`, selecciona `Primeros auxilios`, rota el emulador y verifica que la consulta/filtro permanece en la pantalla. Repite la prueba manualmente si reentras desde otro flujo, ya que el ciclo de vida del ViewModel depende de su propietario.

## Observación académica

Esta entrega incluye código y pruebas de ViewModel. Su presencia no equivale a que estén ejecutados con éxito en Android Studio: conserva la captura de la consola de pruebas como evidencia objetiva.
