# COTEJO 14 — ViewModel y el estado en Compose

**Fuente del cotejo:** capturas del codelab «ViewModel y el estado en Compose» del curso Android Basics with Compose. Se aplican los conceptos, no el tema narrativo ni el código literal del juego de palabras del ejemplo.

## Resultado de la revisión previa (versión 13)

- `auth/AuthViewModel.kt`: ya hereda de `ViewModel` y se encarga de HU01 (registro, inicio de sesión, errores y recuperación).
- `auth/AuthUiState.kt`: ya es un `data class` inmutable.
- `FarmAppRoot.kt`: ya usa `collectAsStateWithLifecycle()` para observar `AuthViewModel.uiState`.
- `AuthRepository` y `FirebaseAuthRepository`: la lógica de datos se mantiene fuera de la UI.
- **Faltaba en HU03 didáctico:** el catálogo contenía `rememberSaveable` para consulta y categoría, instanciaba `CatalogAnalyzer` en la propia pantalla y calculaba los resultados en la composición.

## Cambios de la versión 14

| Concepto del codelab | Aplicación verificable | Archivo |
|---|---|---|
| ViewModel mantiene el estado de UI | `CatalogViewModel` maneja consulta, categoría y resultados | `app/src/main/java/com/example/farmapp/catalog/CatalogViewModel.kt` |
| UI State inmutable | `CatalogUiState` tiene los datos necesarios para dibujar el catálogo | `.../catalog/CatalogUiState.kt` |
| Encapsular el estado mutable | `_uiState` privado y `uiState` público `StateFlow` | `CatalogViewModel.kt` |
| Recolectar con Compose | `collectAsStateWithLifecycle()` | `ui/catalog/CatalogLearningPreview.kt` |
| Estado baja, eventos suben | `CatalogScreenContent(uiState, onQueryChanged, onCategorySelected)` | `ui/catalog/CatalogLearningPreview.kt` |
| Conservar información en recreaciones de Activity | Un `ViewModel` asociado a Activity o NavBackStackEntry conserva estado; `SavedStateHandle` conserva filtros pequeños ante recreación de proceso con estado guardado | `CatalogViewModel.kt` |
| Pruebas JVM del ViewModel | 7 tests unitarios de estado, filtros y restauración simulada | `app/src/test/java/com/example/farmapp/catalog/CatalogViewModelTest.kt` |
| Pruebas instrumentadas | Callback de búsqueda actualiza StateFlow y UI | `app/src/androidTest/java/com/example/farmapp/ui/catalog/CatalogViewModelUiTest.kt` |

## Separación de responsabilidades

1. **Repositorio:** `DemoCatalogData.repository` aporta productos ficticios.
2. **Dominio:** `CatalogAnalyzer` filtra, agrupa y suma resultados.
3. **ViewModel:** `CatalogViewModel` responde a eventos, procesa datos y publica `CatalogUiState`.
4. **Route en Compose:** `CatalogLearningPreview` observa el `StateFlow` durante los estados activos del ciclo de vida.
5. **UI sin estado de negocio:** `CatalogScreenContent` recibe el estado y emite eventos hacia su propietario.

### Flujo unidireccional

`Usuario escribe → onQueryChanged → CatalogViewModel → StateFlow<CatalogUiState> → collectAsStateWithLifecycle → recomposición de CatalogScreenContent`.

## Relación con el ejemplo del curso

El codelab utiliza un juego de palabras y una puntuación acumulada. FarmApp **no copia un juego**: el equivalente es la lista de medicamentos de demostración, el texto de búsqueda, la categoría seleccionada y la cantidad de resultados. Los estados son controlados desde el ViewModel y no desde la UI.

## Limitaciones importantes

- `ViewModel` conserva su instancia durante cambios de configuración ordinarios, no garantiza supervivencia si el proceso se elimina. `SavedStateHandle` ayuda a restaurar **solo** los filtros que fueron guardados cuando corresponde.
- El estado de expansión temporal de cada tarjeta sigue en `rememberSaveable`, pues es **estado local de un componente**, no estado de negocio compartido.
- La persistencia del tema y del inventario real llegará en cotejos/sprints posteriores; el repositorio del catálogo sigue siendo ficticio.
- Esta entrega NO demuestra compilación Android ni ejecución de los tests instrumentados: deben ejecutarse en Android Studio con Gradle y un emulador.

## Demostración para la profesora

1. Abre `CatalogViewModel.kt` y muestra la herencia `: ViewModel()`, `MutableStateFlow` privado y `StateFlow` público.
2. Enseña `CatalogUiState.kt` y su `data class`.
3. Abre `CatalogLearningPreview.kt` y señala `collectAsStateWithLifecycle()` y `CatalogScreenContent()`.
4. Busca un producto y selecciona una categoría; enseña cómo se emiten eventos y se actualiza la UI.
5. Gira el emulador mientras FarmApp está abierta: la búsqueda y categoría deben persistir porque el ViewModel permanece asociado a la misma pantalla/actividad. Comprueba esto en un emulador real.
6. Muestra `CatalogViewModelTest.kt` y `CatalogViewModelUiTest.kt`; ejecuta las pruebas y guarda captura de pantalla de su resultado, si pasan.

## Nota sobre Preview interactiva

`CatalogLearningPreviewPreview()` mantiene un estado temporal **solo en la vista previa** mediante `remember`, para conservar el modo interactivo del Cotejo 08. No modifica la arquitectura de producción: la app real usa `CatalogViewModel` y `StateFlow`.
