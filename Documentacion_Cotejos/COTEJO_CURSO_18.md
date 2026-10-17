# Cotejo N.º 18 — Diseño adaptable del contenido y pruebas multidispositivo

**Curso:** Android Basics with Compose, «Compila una app con un diseño adaptable» (Reply).  
**Punto de partida:** paquete acumulativo del Cotejo 17.  
**Situación:** implementación de código realizada; compilación y pruebas Android pendientes.

## Comparación con el curso

| Requisito | C17 | C18 | Archivo y evidencia |
|---|---|---|---|
| Navegación adaptable (Bar / Rail / Drawer) | Sí | Conservada | `ui/navigation/FarmAppAdaptiveNavigation.kt` |
| `@Preview` para teléfonos y tablets | Solo navegación | Ampliadas al catálogo | `ui/navigation/CatalogRouteScreen.kt` |
| Contenido adaptable lista-detalle | Faltaba | Añadido | `CatalogAdaptiveContent()` |
| Diseño que mide ancho disponible | Faltaba | Añadido | `BoxWithConstraints`, `catalogContentLayoutFor()` |
| Mantener UI y ViewModel separados | Sí | Conservado | `CatalogRouteScreen`, `CatalogAdaptiveContent`, `CatalogViewModel` |
| Una sola lista vertical para productos | Sí | Conservada | `CatalogScreenContent` / `LazyColumn` |
| Panel con ficha reutilizable | Faltaba | Añadido | `ProductDetailBody` en `ProductDetailScreen.kt` |
| Pruebas de umbrales | Faltaba | 6 añadidas | `src/test/.../CatalogContentLayoutTest.kt` |
| Pruebas Compose de contenido adaptable | Faltaba | 5 añadidas | `src/androidTest/.../CatalogAdaptiveContentTest.kt` |

## Arquitectura y decisiones

1. La navegación por tamaños del **Cotejo 17** se mantiene y decide barra/rail/drawer por `WindowWidthSizeClass`.
2. El contenido del catálogo mide **el ancho real disponible después de restar la navegación** usando `BoxWithConstraints`. Si tiene menos de 720 dp se presenta en un panel; desde 720 dp se muestra lista + detalle simultáneos. Este umbral de contenido es una decisión de FarmApp, no un valor obligatorio del curso.
3. En **un panel**, pulsar «Abrir ficha» navega al destino `product/{sku}` mediante callback (C16).
4. En **dos paneles**, el mismo botón actualiza la selección guardada con `rememberSaveable`; no cambia la ruta ni duplica el NavHost.
5. La ficha de producto es el componente reutilizable `ProductDetailBody`, tanto en el destino individual como en el panel derecho.
6. Los filtros y búsqueda siguen en `CatalogViewModel` y `StateFlow`. El estado efímero del producto seleccionado se conserva localmente mediante `rememberSaveable`.
7. Cada panel tiene desplazamiento independiente y acotado: `LazyColumn` para lista, `Column.verticalScroll` para ficha. No se han anidado scrollers verticales de altura infinita.
8. Las páginas siguen usando productos DEMO ficticios. No se ha implementado todavía el inventario real ni las funciones completas de HU03.

## Vistas previas

En `CatalogRouteScreen.kt`:

- `CatalogCompactContentPreview()` — 390×800 dp; teléfono, lista de una columna.
- `CatalogMediumContentPreview()` — 750×800 dp; tablet mediana, rail + lista de una columna.
- `CatalogExpandedContentPreview()` — 1280×800 dp; tablet grande, drawer + lista y ficha.

Las tres se pueden abrir con **Split/Design** en Android Studio y usar con *Interactive mode*. Estas vistas no requieren Firebase.

## Pruebas

**Locales:** `CatalogContentLayoutTest` contiene 6 métodos JUnit (ancho 390, 600, 719.9, 720, 1040 e infinito), independientes del SDK Android.

**Instrumentadas:** `CatalogAdaptiveContentTest` contiene cinco pruebas para comprobar un panel compacto/mediano, dos paneles en pantalla ancha y diferencias de navegación al seleccionar un producto.

Ejecutar desde Android Studio cuando se integren los archivos y sus dependencias:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

> Ninguna de las cinco pruebas instrumentadas ni la compilación completa del APK se ha ejecutado aquí. La verificación local de política Kotlin tampoco prueba la medición Android en un dispositivo real.

## Guion para exposición

«En el Cotejo 17 adapté la navegación según el tamaño de pantalla. En el 18 adapté también el contenido: en celulares abro la ficha en una pantalla separada, mientras que en tablets anchas veo la lista y los detalles al mismo tiempo. Uso `BoxWithConstraints` para decidir según el ancho disponible, preservo el ViewModel para la búsqueda, y pruebo las reglas de tamaño con JUnit y Compose.»
