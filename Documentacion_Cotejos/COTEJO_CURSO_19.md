# Cotejo 19 — Introducción a las corrutinas de Kotlin

**Curso:** Android Basics with Compose, introducción a corrutinas, suspensión, `launch`, `async`,
concurrencia estructurada, `Job`, contexto, dispatcher, cancelación y errores.

## Comparación con la versión 18

| Concepto | Antes | Ahora / archivo |
|---|---|---|
| `suspend` | Sí, repositorio Firebase | También `catalog/CatalogAvailability.kt` |
| `viewModelScope.launch` | Sí, `auth/AuthViewModel.kt` | También `catalog/CatalogAvailabilityViewModel.kt` |
| `CoroutineScope` y `Job` | Parcial: viewModelScope | Job controlado por el ViewModel; hijos ligados a `coroutineScope` |
| `async` + `awaitAll` | Faltaban | `CatalogAvailabilityLoader.load()` |
| `Dispatchers` y `CoroutineContext` | Parcial | `withContext(computationDispatcher)`, inyectable para tests |
| Cancelación | Faltaba una demostración visual | Botón Cancelar, cancela el Job y sus hijos |
| Error de corrutina | `catch(Exception)` podía atrapar cancelaciones | Separado `catch(CancellationException) { throw ... }` en autenticación y catálogo |
| Pruebas deterministas de concurrencia | Faltaban | `CatalogAvailabilityTest.kt` con tiempo virtual y fakes |

## Qué hace la nueva funcionalidad

En **Catálogo**, la tarjeta «Consulta asíncrona de inventario (demo)» inicia tres
solicitudes suspendibles para categorías de productos ficticios y combina los resultados.

- `LocalDemoAvailabilitySource.unitsFor()` ejecuta `delay` y **no bloquea el hilo**.
- `CatalogAvailabilityLoader.load()` usa `withContext`, `coroutineScope`, `async` y `awaitAll`.
- El ViewModel inicia un `Job` hijo de `viewModelScope`; no crea scopes globales.
- El botón Cancelar detiene la operación y muestra el estado `CANCELLED`.
- Las fallas reales se muestran como `ERROR` sin exponer trazas o detalles sensibles.
- Si una consulta falla, `coroutineScope` cancela las restantes (concurrencia estructurada).
- Las pruebas incluyen éxito, error, reintento, consulta duplicada y cancelación.

**No existe conexión a inventario real**: solo se consultan las mismas seis muestras
ficticias de Cotejo 07. Esto no implementa HU03 completa ni HU06.

## Arquitectura y archivos

- `catalog/CatalogAvailability.kt`: fuente suspendible, data class y agregación concurrente.
- `catalog/CatalogAvailabilityViewModel.kt`: estados `IDLE/LOADING/SUCCESS/CANCELLED/ERROR` y Job.
- `ui/catalog/CatalogAvailabilityPanel.kt`: UI Compose sin estado de negocio.
- `ui/navigation/CatalogRouteScreen.kt`: une StateFlow y callbacks a catálogo adaptable.
- `auth/AuthViewModel.kt`: evita transformar CancellationException en error de login.
- `res/values/strings.xml`: textos accesibles y traducibles.
- `test/.../CatalogAvailabilityTest.kt`: 7 casos locales JUnit.
- `androidTest/.../CatalogAvailabilityPanelTest.kt`: 2 casos Compose.

## Nota para la profesora

Una corrutina puede suspenderse, lo que **no** significa que cree obligatoriamente un hilo.
`launch` inicia el proceso vinculado al ViewModel; `async` crea hijos que entregan
resultados; `coroutineScope` espera a sus hijos y propaga cancelación/errores.
`Dispatchers.Default` aquí se usa para aprender el cambio de contexto en el
cálculo; no es necesario para un simple `delay`.

## Alcance de verificación

- Fuentes de los cotejos 01–18 conservadas.
- Verificación estática de imports y XML; compilación aislada JVM de lógica Kotlin con coroutines-core.
- Pruebas JUnit y UI instrumentadas **pendientes** de ejecutar con Gradle y Android Studio.
- No se ha compilado un APK completo ni se ha probado en emulador.
