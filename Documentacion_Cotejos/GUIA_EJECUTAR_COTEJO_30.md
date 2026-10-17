# Guía — Cotejo 30: Android Views, Fragment, Navigation, FAB y diálogo

1. Abre el proyecto FarmApp ya configurado en Android Studio.
2. Copia/fusiona `app/src/main/java`, `app/src/main/res` y `AndroidManifest.xml`; **no reemplaces tu manifest existente sin fusionarlo**.
3. En el bloque `android` de `app/build.gradle.kts`, activa `buildFeatures { viewBinding = true }` (conservando `compose = true`).
4. En `dependencies`, añade las bibliotecas indicadas en `CONFIGURACION_GRADLE_COTEJO_30.md`.
5. Sincroniza Gradle: esto generará las clases `...Binding`.
6. Ejecuta FarmApp, inicia sesión y en Inicio presiona `Inventario con objetos View (demo)`.
7. Pulsa el FAB `+`, escribe nombre, precio y cantidad, y guarda. Debe aparecer la nueva fila.
8. Presiona la fila recién creada: el mismo diálogo se abre con los campos completos para editarla.
9. Regresa a la pantalla Room con Compose para observar el registro actualizado.
10. Abre `res/navigation/legacy_inventory_graph.xml` en vista `Design` o `Split` para mostrar el grafo y `itemId`.
11. Ejecuta `LegacyEntryModeTest` (local) y las pruebas instrumentadas `LegacyInventoryViewsTest` y `LegacyNavigationGraphTest` (emulador).

## Si hay errores

- `Unresolved reference: ...Binding`: falta activar `viewBinding = true` o falta sincronización.
- Error en Navigation XML: agrega `navigation-fragment-ktx` con la misma versión de Navigation Compose.
- Error sobre tema MaterialComponents: comprueba la dependencia Material y que `LegacyInventoryActivity` usa `Theme.FarmApp.Legacy`.
- Si la lista está vacía, añade artículos ficticios: ambos módulos comparten Room y la lista se actualiza con Flow.
- El módulo usa Room, por lo que también necesita todas las dependencias KSP/Room del Cotejo 25.

## Estado

No se ha realizado una compilación Android/Gradle ni una ejecución de emulador en este entorno. Es necesario validar las pruebas en Android Studio antes de la entrega académica.
