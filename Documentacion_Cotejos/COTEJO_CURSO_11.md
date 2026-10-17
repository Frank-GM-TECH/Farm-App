# COTEJO N.º 11 — Animaciones simples con Jetpack Compose

**Unidad del curso:** animaciones en Compose, siguiendo el ejemplo de revelar u ocultar información con una transición tipo resorte de la aplicación Woof.

## Resultado del cotejo

| Concepto del curso | Antes (cotejo 10) | Ahora (cotejo 11) |
|---|---|---|
| Botón para revelar/ocultar información | No en tarjetas | Implementado |
| `rememberSaveable` y `mutableStateOf` | Sí en formularios y filtros | Reutilizados para cada tarjeta |
| `Modifier.animateContentSize()` | No | Implementado |
| `spring()` | No | Implementado con `DampingRatioMediumBouncy` y `StiffnessLow` |
| Vista previa interactiva | Sí en otras pantallas | Nueva preview de la tarjeta |
| Pruebas UI de expansión | No | Dos pruebas propuestas para `androidTest` |

## Implementación concreta

- **Nuevo:** `app/src/main/java/com/example/farmapp/ui/catalog/AnimatedProductDemoCard.kt`
  - `AnimatedProductDemoCard(product)` muestra el producto y añade **Ver detalles / Ocultar detalles**.
  - La tarjeta ajusta su tamaño con `animateContentSize(animationSpec = spring(...))`.
  - El estado `expanded` se conserva con `rememberSaveable(product.sku)` y está asociado al producto.
  - Cuando se expande, aparecen categoría, estado de inventario simulado y advertencia educativa.
  - `AnimatedProductDemoCardPreview()` demuestra el efecto desde **Interactive mode** en Android Studio.
- **Modificado:** `ui/catalog/CatalogLearningPreview.kt`: las tarjetas dentro de `LazyColumn` ahora usan `AnimatedProductDemoCard`; siguen vigentes claves estables, búsqueda y filtros.
- **Modificado:** `res/values/strings.xml`: nuevas etiquetas en español.
- **Nuevo:** `app/src/androidTest/java/com/example/farmapp/ui/catalog/AnimatedProductDemoCardTest.kt`.

## Fragmento para explicar a la profesora

```kotlin
var expanded by rememberSaveable(product.sku) { mutableStateOf(false) }
Column(
    modifier = Modifier.animateContentSize(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
) {
    OutlinedButton(onClick = { expanded = !expanded }) {
        Text(if (expanded) "Ocultar detalles" else "Ver detalles")
    }
    if (expanded) { /* detalles del producto */ }
}
```

El ejemplo anterior es abreviado; el archivo real utiliza `stringResource(R.string...)` en vez de cadenas literales para facilitar traducciones.

## Pasos de demostración en Android Studio

1. Abre `AnimatedProductDemoCard.kt`.
2. Ejecuta `AnimatedProductDemoCardPreview()` desde **Split/Design → Interactive mode**.
3. Pulsa **Ver detalles** y observa la expansión con efecto de resorte; pulsa **Ocultar detalles**.
4. En `CatalogLearningPreview.kt`, muestra cómo `LazyColumn` usa la tarjeta animada como ítem personalizado.
5. Abre `AnimatedProductDemoCardTest.kt` para explicar las aserciones y ejecución instrumentada.
6. Ejecuta las pruebas en un emulador: `./gradlew connectedDebugAndroidTest` (Windows: `gradlew.bat connectedDebugAndroidTest`).

## Dependencia

En el `app/build.gradle.kts` del proyecto anfitrión, verifica:

```kotlin
implementation(platform(libs.androidx.compose.bom)) // Si usas el version catalog de Android Studio
implementation("androidx.compose.animation:animation")
```

Si tu template ya gestiona Compose BOM con otro identificador, conserva el de tu proyecto en lugar de copiar a ciegas ese ejemplo.

## Alcance y verificaciones

- Se preservan archivos previos de HU01, HU03 didáctica y cotejos 01–10.
- El catálogo sigue siendo **ficticio y sin compras**; la animación no conecta con Firebase.
- Se verificaron XML, referencias `R.string`, uso de APIs y empaquetado ZIP por análisis estático. Además se reejecutaron 9 verificaciones de lógica del catálogo en la JVM local, con resultado satisfactorio.
- **Pendiente:** ejecutar la compilación completa de Android, previsualización interactiva y ambas pruebas instrumentadas en Android Studio. No se declara su aprobación sin ejecutarlas.
- El ZIP sigue siendo un conjunto de fuentes que requiere un proyecto Compose anfitrión, no un proyecto Gradle completo.
