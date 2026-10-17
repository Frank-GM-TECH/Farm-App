# COTEJO N.º 23 — Cómo cargar y mostrar imágenes de Internet

## Sección del curso
«Cómo cargar y mostrar imágenes de Internet», aplicación Mars Photos. Conceptos: Coil (`AsyncImage`), `ImageRequest`, `LazyVerticalGrid`, `GridCells.Adaptive`, placeholders/errores, accesibilidad, estados y botón de reintento, pruebas de UI y ViewModel.

## Diagnóstico de la versión 22
- Ya había REST con Retrofit, `@Serializable`, repositorio, UI State, `ViewModel.Factory` e inyección manual.
- No se recogía `thumbnail` desde el JSON, no existía Coil ni cuadrícula de imágenes.
- Sí existía lista vertical del catálogo: no debía anidarse una cuadrícula scrollable en ese contenedor.

## Mejoras incorporadas

| Concepto | Archivo y aplicación |
|---|---|
| `thumbnail` como propiedad JSON opcional | `network/RemoteProductDto.kt` |
| URL de imagen externa (solo HTTPS) | `network/RemoteImageUrl.kt` |
| `AsyncImage` Coil, `ImageRequest`, crossfade y `ContentScale.Crop` | `ui/catalog/RemoteProductGalleryScreen.kt` |
| Placeholder/imagen de error en drawable | `res/drawable/ic_remote_photo_placeholder.xml`, `ic_remote_photo_error.xml` |
| `LazyVerticalGrid(GridCells.Adaptive(minSize = 150.dp))` | `RemoteProductGalleryScreen.kt` |
| `items` con claves estables id | `RemoteProductGalleryScreen.kt` |
| Estados inicial, carga, éxito, vacío y error, botón Reintentar | `RemoteProductGalleryScreen.kt` + `RemoteProductsViewModel.kt` |
| Navegación a galería | `FarmAppDestinations.kt` + `FarmAppNavHost.kt` |
| Callback desde la sección externa del catálogo | `RemoteProductsPanel.kt` + `CatalogRouteScreen.kt` |
| Pruebas sin red | `RemoteProductsImageMappingTest.kt`, `RemoteProductsViewModelTest.kt` y `RemoteProductGalleryScreenTest.kt` |

## Nota importante
La nueva galería está en una **pantalla propia** para evitar introducir un `LazyVerticalGrid` con desplazamiento vertical dentro de la `LazyColumn` existente. Así conserva el diseño adaptable del Cotejo 18.

## Requisitos pendientes para Android Studio

- Agregar dependencia `implementation("io.coil-kt:coil-compose:2.7.0")` a `app/build.gradle.kts` usando una versión compatible con el proyecto real.
- Reutilizar los ajustes Retrofit, kotlinx.serialization, Firebase y Navigation de los cotejos anteriores.
- Compilar con Gradle, ejecutar la galería en emulador con conexión y sin conexión, y correr pruebas locales e instrumentadas.
- En `app/src/main/AndroidManifest.xml` ya existe el permiso `android.permission.INTERNET`.
- API externa: DummyJSON, datos de demostración. No se integran a inventario farmacéutico ni se presentan como medicamentos reales.

## Cómo exponerlo
1. Explicar que el JSON de DummyJSON contiene el campo `thumbnail` y que `@SerialName` hace el mapeo de la propiedad.
2. Mostrar `RemoteProductsRepository` y el ViewModel inyectado por la fábrica del Cotejo 22.
3. Enseñar `AsyncImage`, `placeholder`, `error`, `fallback`, `contentDescription` y `LazyVerticalGrid`.
4. Ejecutar el flujo «Inicio → Catálogo → Ver galería de imágenes».
5. Activar modo avión y comprobar que `Reintentar` mantiene la aplicación estable.
6. Mostrar las vistas previas de éxito/error y las pruebas. **No declarar la suite aprobada antes de ejecutarla en Gradle.**
