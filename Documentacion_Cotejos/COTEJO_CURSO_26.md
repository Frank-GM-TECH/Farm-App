# COTEJO CURSO 26 — Lee y actualiza datos con Room

## Referencia del curso

Google Android Basics with Compose: [Lee y actualiza datos con Room](https://developer.android.com/codelabs/basic-android-kotlin-compose-update-data-room?hl=es-419).
Continuación del codelab Inventory: inventario, detalle, edición y venta de artículos.

## Diagnóstico sobre C25

Ya se implementaron `@Entity`, `@Dao`, `RoomDatabase`, `Flow` para el inventario, formulario para crear y botones para aumentar/eliminar. **Faltaban la navegación a detalle, edición de tres campos y operación de venta con stock protegido.**

## Implementación C26

- `RoomDemoDao.kt`: `observeById(id)` devuelve `Flow<RoomDemoItem?>`, `sellOneIfAvailable(id)` ejecuta **UPDATE atómico condicionado a `stock > 0`** y devuelve filas afectadas, `deleteById` elimina por ID.
- `RoomDemoRepository.kt`: observa un elemento por ID, actualiza campos, vende y elimina; mantiene funciones anteriores para no interrumpir el C25.
- `RoomDemoDetailViewModel.kt`: recibe solo `itemId` desde `SavedStateHandle`; sus estados incluyen carga, error, venta, edición y eliminación, y observa el `Flow` de Room.
- `RoomDemoInventoryRules.kt`: regla Kotlin sin Android para deshabilitar ventas cuando el stock es cero.
- `RoomDemoDetailScreen.kt`: ficha detallada con acciones Vender 1, Editar y Eliminar, diseño sin NavController acoplado.
- `RoomDemoEditScreen.kt`: formulario precargado para nombre, precio y stock, con valores guardados mediante `rememberSaveable` y validación existente `RoomDemoForm`.
- `RoomDemoScreen.kt`: añade Ver detalles a cada fila.
- `FarmAppDestinations.kt` y `FarmAppNavHost.kt`: rutas protegidas `room_demo/{itemId}` y `room_demo/{itemId}/edit` (`NavType.LongType`), callbacks Atrás y navegación entre pantallas.
- `strings.xml`: todos los textos nuevos externalizados.

**No cambiamos** `RoomDemoItem` ni la versión 1 de `RoomDemoDatabase`; por tanto no se requiere migración de esquema. Se mantiene el archivo `farmapp_room_demo.db` y los datos existentes. La base SQLite independiente de C24 también se conserva.

## Por qué SQL atómico para vender

```sql
UPDATE articulos_room_demo
SET stock = stock - 1
WHERE id = :id AND stock > 0;
```

Con dos intentos consecutivos cuando queda 1 unidad, el primero modifica una fila (venta) y el segundo modifica 0 filas (agotado). Se evita el patrón inseguro leer + restar + actualizar con riesgo de carrera.

## Criterios de cotejo y evidencia

| Concepto del curso | Archivo | Estado |
|---|---|---|
| Lectura reactiva de inventario | `RoomDemoDao.kt`, `RoomDemoViewModel.kt` | Ya incluido C25 |
| Lectura individual con `Flow` | `RoomDemoDao.kt`, `RoomDemoDetailViewModel.kt` | Añadido |
| Navegación a detalle por ID | `FarmAppNavHost.kt` | Añadido |
| UI de detalle | `RoomDemoDetailScreen.kt` | Añadido |
| UI de edición y validación | `RoomDemoEditScreen.kt` | Añadido |
| Venta con descontar stock | `RoomDemoDao.kt` | Añadido |
| Venta deshabilitada sin stock | `RoomDemoInventoryRules.kt` y UI | Añadido |
| Eliminar por ID | `RoomDemoDao.kt` y DetailViewModel | Añadido |
| Test DAO real | `RoomDemoInventoryDaoTest.kt` | Añadido; ejecución Android pendiente |
| Test ViewModel fake | `RoomDemoDetailViewModelTest.kt` | Añadido; ejecución Gradle pendiente |
| Pruebas de UI | `RoomDemoDetailsScreenTest.kt` | Añadido; emulador pendiente |

## Alcance

Los registros de este laboratorio son **ficticios**. La venta solo disminuye el stock de ejemplo: no cobra, no crea pedidos, no escribe auditoría contable y no altera el inventario real. Más adelante una operación de venta real requerirá transacción entre stock, pedido y comprobante.

## Limitaciones de validación

Se revisó la estructura y se añadieron pruebas. Las pruebas Android/Room/KSP, compilación APK, Database Inspector y validación en emulador siguen pendientes. Este paquete es código fuente para integrar en un proyecto Gradle Compose, no un proyecto Gradle independiente.
