# Guía práctica — Cotejo 08 (Card, LazyColumn y elementos personalizados)

## Integración en Android Studio
1. Crea/abre un proyecto **Empty Activity — Kotlin + Jetpack Compose** con `com.example.farmapp`.
2. Copia `app/src` del ZIP de cotejo 08 sobre el módulo `app/` de tu proyecto (fusiona, no borres los archivos Gradle de la plantilla).
3. Sigue `README.md` para dependencias y configuración de Firebase; la **Preview del catálogo no necesita Firebase**.
4. Abre `ui/catalog/CatalogLearningPreview.kt` y despliega la función `CatalogLearningPreviewPreview()` en la pestaña Preview (Design/Split).
5. Comprueba que hay tarjetas por categoría, desplázate, filtra Higiene y busca "gasas".

## Dónde explicar el curso
- `LazyColumn`: comienzo de `CatalogLearningPreview()`.
- `item()`: cabecera, controles, grupos y pie de pantalla.
- `items()`: una tarjeta por cada `DemoProduct` de la colección filtrada y agrupada.
- `key={ product -> product.sku }`: identidad estable de cada producto.
- `ProductDemoCard()`: diseño personalizado con `Card`, `Column` y `Text`.
- `HomeScreen`: una sola lista vertical; no `Column(verticalScroll())` envolviendo otra lista.

## Pruebas reales (emulador Android iniciado)
- Windows: `./gradlew.bat :app:connectedDebugAndroidTest` (PowerShell acepta también ` .\gradlew.bat ... `).
- JUnit local: `./gradlew.bat :app:testDebugUnitTest`.
- En el panel *Run*, comprueba los nombres y resultados reales. Las pruebas añadidas se llaman `buscarPorNombreFiltraLaVista`, `filtroPorCategoriaSePuedeRestablecer` y `lazyColumnPermiteDesplazarseHastaUnaTarjetaPersonalizada`.
- Si el emulador no muestra un elemento al iniciar, es normal en una lista lazy: desplázate hasta verlo.

**Aviso:** este ZIP contiene fuentes Android para integrar, no incluye wrapper Gradle ni un APK listo para ejecutar. La compilación completa y las pruebas instrumentadas siguen pendientes en un proyecto Android Studio real.
