# COTEJO 31 — Agregar Compose a una aplicación basada en Views

## Codelab oficial
«Cómo agregar Compose a una app basada en objetos View» — Juice Tracker (Android Basics with Compose, Unidad 8).
https://developer.android.com/codelabs/basic-android-kotlin-training-compose-add-compose-to-a-view-based-app?hl=es-419

## Estado previo (Cotejo 30)
El módulo `ui/legacy` era 100 % View: `Fragment`, `RecyclerView`, un layout `row_legacy_item.xml` con dos `TextView`, FAB y diálogo XML. Ya usaba artículos ficticios del laboratorio Room (Cotejos 25 y 26).

## Cambios realizados

| Competencia del curso | Dónde está en FarmApp | Resultado |
|---|---|---|
| Usar Compose en una UI basada en View | `res/layout/row_legacy_item.xml` incluye `androidx.compose.ui.platform.ComposeView` | Incorporado |
| Convertir elemento de lista XML a Compose | `LegacyInventoryComposeRow.kt` | Incorporado |
| Reutilizar Material 3 | `FarmAppTheme`, `Card`, `Text`, `Row`, `Column` | Incorporado |
| Gestionar Compose dentro de RecyclerView | `LegacyInventoryAdapter.kt`: `ItemHolder`, `setViewCompositionStrategy` y `setContent` | Incorporado |
| Estado y recomposición | `ItemHolder.currentItem` observable mediante `mutableStateOf`, actualizado con DiffUtil/Room | Incorporado |
| Eventos de interfaz con callbacks | `onClick` que remite el ID al `Fragment` y su `NavController` | Incorporado |
| Vista previa Compose | `LegacyInventoryComposeRowPreview()` | Incorporado |
| Pruebas automatizadas | `LegacyInventoryComposeRowTest.kt`: renderizado, clic e inserción de estado | Incluidas, no ejecutadas en Android |

## Recorrido de los datos

```
Room (Flow) → LegacyTrackerFragment → RecyclerView/ListAdapter
                                             ↓
                                     ViewHolder / ComposeView
                                             ↓
                                LegacyInventoryComposeRow(item)
                                             ↓ clic
                               Fragment.findNavController().navigate()
                                             ↓
                                DialogFragment XML / Room
```

Se evita reconstruir la composición en cada `onBindViewHolder`: `setContent` se configura una sola vez por `ViewHolder` y se actualiza el estado mutable `currentItem`. La estrategia `DisposeOnDetachedFromWindowOrReleasedFromPool` se recomienda para `ComposeView` en un contenedor reciclable.

El `Fragment`, `RecyclerView`, `FloatingActionButton`, grafo XML de navegación, diálogos XML, repositorios Room y el resto de la app en Compose permanecen operativos a nivel de implementación. Solo cambiamos **cómo se dibuja una fila**.

## Explicación para la profesora

1. Enseña el `TextView` tradicional del cotejo anterior y el `ComposeView` de `row_legacy_item.xml` del actual.
2. En `LegacyInventoryAdapter.kt` señala `setContent`, `mutableStateOf` y `ViewCompositionStrategy`.
3. En `LegacyInventoryComposeRow.kt` identifica `@Composable`, `Card`, `Row`, `Column`, `Text`, `onClick` y `@Preview`.
4. Prueba añadir y editar artículos desde el botón flotante y la fila Compose; el diálogo sigue basado en Views.
5. Actualiza existencias desde la pantalla Room hecha en Compose y verifica que la fila del RecyclerView refleje el cambio.

## Verificación y limitaciones

- Integridad de fuentes y XML: revisada.
- Se verificaron las rutas, etiquetas de recursos y la continuidad de todos los archivos anteriores.
- Pruebas instrumentadas y compilación Gradle Android completa **pendientes**.
- Paquete de fuentes para integrar en un proyecto Android Studio (NO proyecto Gradle autónomo).
