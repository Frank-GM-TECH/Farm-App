# GUÍA COTEJO 11 — ANIMACIÓN DE RESORTE

1. Abre el proyecto FarmApp en Android Studio y copia los archivos de `app/src` de este paquete, conservando la estructura.
2. Agrega `implementation("androidx.compose.animation:animation")` al `app/build.gradle.kts` si todavía no está incluido; usa la Compose BOM de tu proyecto.
3. Sincroniza Gradle. Abre `ui/catalog/AnimatedProductDemoCard.kt` y pulsa **Preview**.
4. Usa **Interactive mode** para comprobar el botón **Ver detalles / Ocultar detalles** y el efecto elástico.
5. También puedes entrar al catálogo en la pantalla tras iniciar sesión y expandir distintos productos.
6. Abre `AnimatedProductDemoCardTest.kt` y ejecuta las pruebas instrumentadas con un emulador Android en marcha.

**Explicación:** una variable `expanded` de Compose cambia por el botón, el árbol de UI se recompone y `animateContentSize(spring(...))` interpola la altura de la tarjeta. Cada producto posee su propio estado.

**Importante:** falta la compilación y ejecución real en Android Studio; el paquete incluye fuentes y recursos, no un proyecto Gradle standalone.
