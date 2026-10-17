# COTEJO 32 — Views dentro de Jetpack Compose

## Codelab
**Usa la interoperabilidad con objetos View en Compose** (Juice Tracker, Unidad 8 de Android Basics with Compose).

## Diagnóstico del código anterior
- Cotejo 30: la actividad y los fragmentos tradicionales XML usan `DialogLegacyItemBinding`, `EditText`, `RecyclerView` y Navigation Fragment.
- Cotejo 31: las filas del RecyclerView se dibujan con `ComposeView` y `LegacyInventoryComposeRow`.
- **Faltaba el camino inverso**: incrustar y conectar Views XML dentro de una UI Compose mediante `AndroidViewBinding`.

## Integración aplicada al proyecto
- Se reutiliza **el mismo XML `dialog_legacy_item.xml`** del cotejo 30, sin duplicar el formulario ni cambiar Room.
- Nueva función `LegacyRoomEditDialog` en `ui/interop/LegacyRoomEditDialog.kt`: `AlertDialog` Compose contiene `AndroidViewBinding(DialogLegacyItemBinding::inflate)`.
- `RoomDemoDetailScreen.kt` muestra el botón `Editar con formulario XML en Compose` y lo conecta a `RoomDemoDetailViewModel.update` mediante `onSaveWithViews`.
- Al abrir el diálogo, sus campos `EditText` se rellenan con el artículo actual; sus eventos de texto elevan el estado hacia Compose.
- `RoomDemoForm.validate` valida antes de invocar el ViewModel; el ViewModel también valida y persiste por Room.
- Los observadores `TextWatcher` se instalan una sola vez por formulario inflado y no se multiplican en recomposiciones.
- XML `res/values/ids.xml` para marcar la inicialización del formulario; tres recursos de texto nuevos.
- Se conserva el editor Compose completo del cotejo 26 y la Activity tradicional del cotejo 30.

## Matriz de concordancia

| Requisito | Evidencia |
|---|---|
| Views XML dentro de Compose | `LegacyRoomEditDialog.kt`, `AndroidViewBinding` |
| Android ViewBinding | `DialogLegacyItemBinding::inflate` |
| Diseño XML reutilizado | `res/layout/dialog_legacy_item.xml` |
| Mostrar cuadro de diálogo de entrada | `AlertDialog` + botón en `RoomDemoDetailScreen.kt` |
| Datos de Room | `RoomDemoDetailViewModel.update` -> repositorio Room |
| Validación | `RoomDemoForm.validate` |
| Interacción en ambos sentidos | `EditText` -> `mutableStateOf` -> callback de guardado |
| UI declarativa Compose anterior | Conservada |
| Pruebas | `LegacyRoomEditDialogTest.kt` (instrumentadas), `Room32PriceTextTest.kt` (locales) |

## Demostración ante la profesora
1. Abre FarmApp -> Laboratorio Room -> agrega un artículo -> abre detalles.
2. Comprueba el botón **Editar con formulario XML en Compose**.
3. Cambia nombre, precio y stock; guarda; verifica la actualización en la ficha y Database Inspector.
4. Abre `LegacyRoomEditDialog.kt` y señala `AndroidViewBinding(DialogLegacyItemBinding::inflate)`.
5. Muestra `dialog_legacy_item.xml` y compara el `DialogFragment` XML del cotejo 30 con el diálogo Compose del cotejo 32.
6. Ejecuta las pruebas instrumentadas para probar edición, validación y cancelación.

## Límites de verificación
Código y referencias inspeccionados; la prueba Android Gradle con Compose UI, ViewBinding y KSP, así como el emulador y Database Inspector, siguen pendientes. El ZIP no es un proyecto Gradle autónomo.
