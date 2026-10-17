# Cotejo 07 — Nuevas características de Kotlin y colecciones en FarmApp

## Objetivo y alcance
Se compararon tres resúmenes del curso Android Basics with Compose: genéricos/clases enum/data/singleton/companion/extensiones/scope functions; Array/List/Set/Map; forEach/interpolación/map/filter/groupBy/fold/sortedBy.

## Inspección del Cotejo 06
- **Existían:** `data class AuthUiState`, `PasswordAssessment`, `enum PasswordStrength`, `companion object` en `AuthViewModel`, List y map, lambda y algunas funciones de alcance. Además ya existían pruebas JUnit y Compose.
- **No existía un ejemplo específico de catálogo** para genéricos, singleton de datos, extensiones, Array/Set/Map, filtrado + agrupación + cálculos, ni pruebas locales de ese dominio.

## Cobertura de cada concepto (código nuevo)
| Concepto | Archivo y evidencia | Uso en FarmApp |
|---|---|---|
| Clases genéricas | `catalog/CatalogRepository.kt` (`CatalogRepository<T>` e `InMemoryCatalogRepository<T>`), `CatalogAnalyzer<T>` | Datos independientes del origen; reutilizable en HU03 |
| `enum class` | `catalog/DemoProduct.kt`, `DemoCategory`, `StockStatus` | Categorías y disponibilidad |
| `data class` | `DemoProduct`, `CatalogSnapshot<T>` | Modelo y resultados inmutables |
| `object` singleton | `DemoCatalogData`, `DemoStockPolicy` | Datos y clasificación locales de demo |
| `companion object` | `DemoProduct.normalizedSku` | Normalizar código interno |
| Extensiones | `DemoProduct.isAvailable()` | Mostrar producto agotado |
| Scope functions | `with`, `let`, `apply`, `run` en `CatalogAnalyzer.kt` y `also` en `CatalogRepository.kt` | Transformaciones y comprobación de lista demo |
| Array | `IntArray`, `intArrayOf(0, 5)` en `DemoStockPolicy` | Umbrales simulados |
| List | `List<T>` en `CatalogRepository` | Listado ordenado |
| Set | `map { it.category }.toSet()` | Categorías sin duplicados |
| Map | `groupBy` y `unitsByCategory` | Agrupación y unidades por categoría |
| `forEach` | `CatalogAnalyzer` y `CatalogLearningPreview` | Recorrer grupos y mostrar tarjetas |
| Interpolación | `catalog_filter_${category.name}` en test tags | Identificación de filtros (uso interno) |
| `map()` | `CatalogAnalyzer.search` | Categorías |
| `filter()` | `CatalogAnalyzer.search` | Búsqueda y categoría combinadas |
| `groupBy()` | `CatalogAnalyzer.search` | Categorías |
| `fold()` | `CatalogAnalyzer.search` | Unidades y valorización de inventario simulado |
| `sortedBy()` | `CatalogAnalyzer.search` | Productos ordenados por nombre |

## Efecto real en la app
- `HomeScreen.kt` integra `CatalogLearningPreview()` **después de autenticar**, sin romper el inicio de sesión de HU01.
- Campo de búsqueda y chips cambian el resultado mediante recomposición; se usa `rememberSaveable`.
- Tarjetas muestran resultados de ejemplo y alertas ficticias de disponibilidad.
- Todos los títulos, categorías y mensajes de UI nuevos están en `strings.xml`.
- `CatalogLearningPreviewPreview()` permite vista previa sin Firebase.

## Verificaciones y pendientes
- `src/test/.../catalog/CatalogAnalyzerTest.kt`: 13 tests JUnit preparados para Gradle.
- `src/androidTest/.../catalog/CatalogLearningPreviewTest.kt`: 2 tests Compose preparados para emulador.
- Compilación de los archivos Kotlin puros con `kotlinc` y ejecución de casos básicos en JVM: ver `VALIDACION_07.txt`.
- **NO** se ha ejecutado Gradle Android, Android SDK, Firebase ni prueba de interfaz en emulador.
- El catálogo sigue siendo una **demo local**; HU03 real y HU06 inventario persitente siguen pendientes.

## Demostración a la profesora
1. En Android Studio abrir `DemoProduct.kt` y señalar `enum class`, `data class`, `companion object`, `IntArray` y extensión.
2. Abrir `CatalogRepository.kt` y mostrar `CatalogRepository<T>` + `object DemoCatalogData`.
3. Abrir `CatalogAnalyzer.kt` y explicar `List/Set/Map` y las operaciones funcionales.
4. Abrir `CatalogLearningPreview.kt`, ejecutar la vista previa, buscar `gasas` y seleccionar filtro `Primeros auxilios`.
5. Ejecutar las pruebas unitarias y guardar capturas del resultado real, no solo del código.
