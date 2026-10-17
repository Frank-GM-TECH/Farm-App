# COTEJO N.º 16 — Cómo navegar entre pantallas con Compose

**Referencia:** curso Android Basics with Compose, codelab *Cómo navegar entre pantallas con Compose*.

## Revisión del Cotejo 15

- Ya existían dos interfaces (AuthScreen y HomeScreen), pero se alternaban con un `if` en `FarmAppRoot.kt`.
- **No** existían `rememberNavController`, `NavHost`, rutas, navegación de regreso o argumentos de destino.
- Existía el catálogo educativo HU03, incrustado dentro de HomeScreen, sin ruta propia.
- No había `Intent.ACTION_SEND` para compartir una ficha con otra aplicación.

## Implementación del Cotejo 16

| Concepto | Archivo de evidencia | Aplicación |
|---|---|---|
| Rutas centralizadas | `ui/navigation/FarmAppDestinations.kt` | `auth`, `home`, `catalog`, `product/{sku}` |
| Controlador | `ui/navigation/FarmAppNavHost.kt` | `rememberNavController()` o uno inyectado en tests |
| `NavHost` y `composable()` | `ui/navigation/FarmAppNavHost.kt` | Destinos de interfaz |
| Separar navegación de pantallas | `HomeScreen.kt`, `CatalogRouteScreen.kt`, `ProductDetailScreen.kt` | Callbacks, no se pasa `NavController` a las pantallas |
| Argumentos entre pantallas | `FarmAppDestinations.productRoute()` | Viaja solo `sku`, se recupera producto en destino |
| Barra superior + Atrás | `CatalogRouteScreen.kt`, `ProductDetailScreen.kt` | `TopAppBar`, `popBackStack()` |
| Abrir otra app | `FarmAppNavHost.kt` | `Intent.ACTION_SEND` y `Intent.createChooser()` para información ficticia |
| Sesión y pila de regreso | `FarmAppNavHost.kt` | Al entrar/salir se limpia el historial de la sesión |
| Pruebas instrumentadas | `FarmAppNavigationTest.kt` | Inicio, catálogo, detalle, compartir y cierre de sesión |

## Flujo de pantallas

```text
Auth (sin sesión) ──[Firebase autentica]──> Inicio
Inicio ──[Explorar catálogo]──> Catálogo
Catálogo ──[Abrir ficha]──> Detalle SKU
Detalle ──[Atrás]──> Catálogo ──[Atrás]──> Inicio
Detalle ──[Compartir]──> Selector de apps (Intent ACTION_SEND)
Inicio ──[Cerrar sesión]──> Auth (sin back stack protegido)
```

## Notas importantes

- HU01 con Firebase continúa como antes. La autenticación real es necesaria para mostrar Inicio en la app normal.
- El historial se mantiene durante rotaciones cuando la sesión no cambia; al iniciar/cerrar sesión se vacía.
- HU03 sigue siendo **demostración educativa**, sin venta, escáner, inventario real ni backend.
- `onProductSelected` y `onOpenProduct` son opcionales para no romper previews y pruebas anteriores.
- Mantuvimos `LazyColumn`, animaciones, accesibilidad, temas, `StateFlow`, pruebas y los 15 cotejos previos.
- El código incluye el flujo de Intents descrito en el resumen del codelab.
- La compilación Android / navegación en emulador permanece **pendiente**; no se afirma que se haya ejecutado.

## Dependencias que debes añadir en `app/build.gradle.kts`

```kotlin
implementation("androidx.navigation:navigation-compose:2.10.2")
androidTestImplementation("androidx.navigation:navigation-testing:2.10.2")
```

Mantén el Compose BOM de tu plantilla. Navigation 2.10.2 está disponible según las notas de AndroidX de septiembre de 2026.
Documentación: https://developer.android.com/develop/ui/compose/navigation
y https://developer.android.com/guide/navigation/testing/compose?hl=es-419
