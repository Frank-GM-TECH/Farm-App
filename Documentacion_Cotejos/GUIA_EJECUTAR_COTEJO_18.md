# Guía Cotejo 18 — Diseño adaptable en Android Studio

1. Abre la plantilla Compose de FarmApp con paquete `com.example.farmapp`.
2. Fusiona **`app/src`** desde este ZIP, conservando la configuración Gradle y Firebase de tu proyecto local.
3. Sincroniza Gradle. No hay dependencias nuevas frente al Cotejo 17: necesitas Compose Material 3, `lifecycle-viewmodel-compose`, `lifecycle-runtime-compose`, `navigation-compose` y la suite JUnit/Compose Test ya indicada en entregas anteriores.
4. Abre `CatalogRouteScreen.kt` y ubica `CatalogCompactContentPreview()`, `CatalogMediumContentPreview()` y `CatalogExpandedContentPreview()`. Revisa las tres en Design/Split.
5. Abre `CatalogContentLayout.kt` y explica el umbral **720 dp del ancho útil del catálogo** (el panel lateral de navegación ya ocupa parte del ancho).
6. Abre `CatalogAdaptiveContent()` y enseña `BoxWithConstraints` y la decisión entre un panel y lista + detalle.
7. En emulador de teléfono, entra a Catálogo y pulsa Abrir ficha. Debe navegar a la ficha completa; Atrás devuelve al catálogo.
8. En dispositivo redimensionable suficientemente ancho (recomendado 1280 dp), entra a Catálogo. Selecciona una tarjeta; cambia la ficha derecha sin abrir otra ruta.
9. Reduce el ancho hasta que no quepan cómodamente los dos paneles y verifica que la pantalla vuelva a la lista única.
10. Ejecuta `CatalogContentLayoutTest` y `CatalogAdaptiveContentTest`; guarda capturas del resultado **después de la ejecución**, no uses la presencia de los archivos como evidencia de pruebas aprobadas.

**Criterio adicional:** se debe poder leer el contenido con tamaño de fuente grande, sin solapamientos. Validar también pantalla horizontal y lector de pantalla TalkBack. No se ha medido accesibilidad en un dispositivo real.
