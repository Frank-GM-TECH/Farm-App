# Cotejo 27 — Guarda tus preferencias de forma local con DataStore

## Fuente de referencia

Curso oficial de Google Android Basics with Compose: "Guarda tus preferencias de forma local con DataStore".
Esta unidad usa Preferences DataStore para recordar la elección de diseño Lista/Cuadrícula.

## Inspección de la versión 26

- La UI ya usaba tema Material 3 y `ThemeModeSelector`, pero la opción vivía en
  `rememberSaveable`: sobrevivía rotaciones normales, **no** reinicios del proceso.
- La galería externa usaba siempre `LazyVerticalGrid` y no permitía elegir lista.
- `AppContainer` manejaba Firebase, Retrofit, SQL y Room, pero no preferencias DataStore.

## Implementación nueva en FarmApp

| Concepto del codelab | Archivo y evidencia |
|---|---|
| Preferences DataStore, claves | `data/preferences/DataStoreUserPreferencesRepository.kt`: `stringPreferencesKey()` |
| DataStore singleton por Context | mismo archivo: `Context.userPreferencesDataStore by preferencesDataStore(...)` |
| Lectura reactiva | `store.data.catch { ... }.map { ... }` -> `Flow<UserPreferences>` |
| Guardado suspendible | `store.edit { ... }` en `setTheme` y `setGalleryLayout` |
| Interface del repositorio | `data/preferences/UserPreferences.kt` |
| DI manual | `data/AppContainer.kt` + `DefaultAppContainer` con `by lazy` |
| UI State + ViewModel | `data/preferences/UserPreferencesViewModel.kt` (StateFlow, viewModelScope) |
| Tema persistido | `FarmAppRoot.kt` (tema desde ViewModel) |
| Lista/Cuadrícula | `RemoteProductGalleryScreen.kt` (LazyColumn / LazyVerticalGrid) |
| Navegación integrada | `FarmAppNavHost.kt` pasa preferencia a la galería |
| Textos localizados | `res/values/strings.xml` |

## Propiedad y límites de datos

`UserPreferences(theme, galleryLayout)` es pequeño y no sensible. Dos claves String:
`theme_mode` y `gallery_layout`. Ante un valor no reconocido, usa SYSTEM o GRID.
Las preferencias son globales a esta instalación de la app, **no** por usuario Firebase.
No guardar correos, contraseñas, tokens, historiales, recetas ni datos de pacientes.
El inventario de Room C25/C26 y el laboratorio SQLite C24 se conservan.

## Pruebas incorporadas

- `UserPreferencesModelTest`: 4 pruebas JVM para defaults y valores desconocidos.
- `UserPreferencesViewModelTest`: 5 pruebas con repositorio fake y control de corrutinas.
- `DataStoreUserPreferencesRepositoryTest`: 4 pruebas Android con DataStore de archivo de test.
- `GalleryDataStorePreferenceUiTest`: 3 pruebas Compose con datos de galería falsos.
- Además: `ManualDiViewModelTest` fake actualizado con dependencia nueva.

## Riesgos / pendientes

- **Sin compilación Android Gradle completa**. El ZIP solo contiene fuentes y documentos.
- Pruebas JUnit reales y DataStore/Compose en emulador **pendientes**.
- La persistencia después de forzar cierre y reiniciar debe verificarse manualmente.
- Si se cambia el formato o se añaden preferencias por usuario, diseñar migración/aislamiento.
