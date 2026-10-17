# Cotejo 30 — Cómo compilar una app para Android con objetos View

## Parte del curso

Unidad 8, Views y Compose, primera ruta de aprendizaje: interfaces XML, componentes View, Fragment, Navigation, FAB, listeners, diálogo de entrada y paso del identificador del artículo. Se adapta el ejercicio Juice Tracker al **laboratorio educativo de inventario FarmApp**.

## Diagnóstico (versión 29)

- Todas las pantallas estaban implementadas en Jetpack Compose; no existía un Fragment propio con layout XML, RecyclerView y Navigation de fragments.
- La app disponía de una base Room educativa (`articulos_room_demo`) y un repositorio que se pueden reutilizar.
- Ya existía Navigation Compose. **Se conserva** para todo el resto de FarmApp.

## Implementado (versión 30)

| Contenido del curso | Archivo o elemento | Estado |
|---|---|---|
| Crear layouts XML | `res/layout/activity_legacy_inventory.xml`, `fragment_legacy_tracker.xml`, `row_legacy_item.xml`, `dialog_legacy_item.xml` | Incluido |
| Activity con Views | `ui/legacy/LegacyInventoryActivity.kt` | Incluido |
| Fragment | `LegacyTrackerFragment.kt` | Incluido |
| Binding de Views | Clases generadas `FragmentLegacyTrackerBinding`, `RowLegacyItemBinding` y `DialogLegacyItemBinding` | Incluido; requiere `viewBinding = true` |
| RecyclerView / ListAdapter | `LegacyInventoryAdapter.kt` | Incluido |
| FAB + `setOnClickListener` | `LegacyTrackerFragment.kt` | Incluido |
| Navigation XML y destinos | `res/navigation/legacy_inventory_graph.xml` | Incluido |
| `findNavController().navigate()` | `LegacyTrackerFragment.kt` | Incluido |
| DialogFragment para añadir o editar | `LegacyItemDialogFragment.kt` | Incluido |
| Pasar ID al navegar | Argumento tipo Long `itemId`; 0 nuevo y positivo edición | Incluido |
| Reutilización de datos Room | `AppContainer.roomDemoRepository` | Incluido |
| Pruebas JUnit y Espresso | `LegacyEntryModeTest.kt`, `LegacyInventoryViewsTest.kt`, `LegacyNavigationGraphTest.kt` | Añadidas; Gradle/Android pendiente |

### Funcionamiento

Desde el Inicio de Compose se abre la actividad **Inventario con objetos View (demo)**. El fragmento lista los mismos artículos que Room guarda en el Cotejo 25. Pulsar el FAB abre un diálogo sin ID (0), pulsar una fila abre el mismo diálogo con el ID existente. Los cambios se guardan en el repositorio Room y son observables tanto desde Views como desde Compose. El módulo no realiza operaciones sobre existencias reales.

### Diseño deliberado

Se usa una Activity separada para simplificar el primer codelab de View y mantener el NavHost de Compose intacto. Se utiliza el tema XML `Theme.FarmApp.Legacy` solo para esa Activity; el tema Compose existente no se sustituye. No se implementa autenticación ni control de acceso a nivel de Activity, pues muestra exclusivamente datos de laboratorio; no extender esta solución a datos sensibles.

### Demostración docente

1. Muestra los 4 layouts en `res/layout`.
2. Abre el grafo `legacy_inventory_graph.xml`: Fragment y destino `<dialog>` con argumento `itemId`.
3. Abre `LegacyTrackerFragment.kt` y señala `setOnClickListener`, `findNavController` y la colección de `repository.items`.
4. Ejecuta la app, abre el laboratorio Views y utiliza el FAB y una fila existente.
5. Muestra un artículo añadido desde Views en la pantalla Room escrita con Compose.
6. Ejecuta las pruebas instrumentadas y las de lógica.

### Verificación

- Se comprobaron archivos, rutas, referencias `R.string`, XML y empaquetado ZIP.
- Se compilará con Kotlin/JVM de forma aislada el helper `LegacyEntryMode` si existe kotlinc.
- No se ha compilado el proyecto Android completo ni ejecutado Room/Fragments/Espresso con Gradle; estas pruebas quedan pendientes.
- Es un paquete de **fuentes integrables**, NO un proyecto autónomo con Gradle.
