# Cotejo 21 — Cómo obtener datos de Internet (REST, Retrofit y JSON)

**Origen:** curso Android Basics with Compose → «Cómo obtener datos de Internet», app de ejemplo Mars Photos.

## Comparación con FarmApp versión 20

| Tema | Antes del cotejo | Después |
|---|---|---|
| `android.permission.INTERNET` | Ya declarado | Se conserva, no se duplica |
| Conexión real a servicio REST | No, las consultas de C19/C20 son simuladas | Nueva petición GET externa con Retrofit |
| API mediante interfaz con `@GET` | No | `RemoteProductsService.kt` |
| `by lazy` para servicio Retrofit | No había Retrofit | `RemoteProductsServiceProvider` |
| Respuesta JSON → Kotlin (`@Serializable`) | No | `RemoteProductDto.kt` |
| `@SerialName` para claves JSON | No | `title` se mapea a `productName` |
| Repositorio separado de ViewModel | Ya usado localmente | También aplicado a red |
| UI de carga, éxito, vacío, error | En demos locales | Añadido a consulta REST |
| Cancelación en ciclo de vida | Ya vista | Mantiene `viewModelScope` / `CancellationException` |
| Pruebas de parseo, errores, interfaz | Parciales | Diez pruebas locales + cuatro Compose añadidas, sin ejecutar Android |

## Función añadida

El catálogo muestra una tarjeta «Productos externos de cuidado de la piel (REST)» con botones **Consultar Internet** y **Ver JSON** (respuesta como String). El botón consulta `https://dummyjson.com/products/category/skin-care?limit=8`, que sirve **datos ficticios de demostración**. NO son medicamentos autorizados, no constituyen un stock, no son precios peruanos ni deben habilitar compras o recetas.

## Archivos

- `network/RemoteProductDto.kt`: DTO JSON con `@Serializable` y `@SerialName`.
- `network/RemoteProductsService.kt`: `@GET`, `@Query`, Retrofit, `ScalarsConverterFactory` (JSON como texto) y convertidor Kotlinx (objetos).
- `network/RemoteProductsRepository.kt`: capa de datos reemplazable en pruebas.
- `network/RemoteProductsViewModel.kt`: `StateFlow` e indicadores HTTP/Internet/JSON.
- `ui/catalog/RemoteProductsPanel.kt`: estado y botones Compose, accesibilidad.
- `ui/navigation/CatalogRouteScreen.kt`: inserción de la tarjeta en el encabezado del catálogo.
- `res/values/strings.xml`: textos localizables.
- Tests: `RemoteProductsParsingTest`, `RemoteProductsViewModelTest`, `RemoteProductsPanelTest`.

## Evidencia y límites

- Revisar el permiso `INTERNET` en `AndroidManifest.xml`: estaba presente y se conserva.
- No se envían correos, contraseñas ni información personal a DummyJSON. Las peticiones no requieren token.
- No se modifica el catálogo local ni la HU03 real; los productos externos aparecen solamente en la tarjeta educativa.
- La solicitud es manual, con botón Consultar Internet. La preview usa datos fijos y no hace HTTP.
- Probar modo avión para comprobar el error de red y botón Reintentar.
- **Pendiente**: sincronización Gradle, compilación completa Android, ejecución real del GET, pruebas JUnit y Compose en emulador.

## Fuente del curso

https://developer.android.com/codelabs/basic-android-kotlin-compose-getting-data-internet?hl=es-419

## API pública de ejemplo

https://dummyjson.com/docs/products
