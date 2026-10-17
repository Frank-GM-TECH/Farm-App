# FarmApp — Cotejo 12: Accesibilidad en Jetpack Compose

## Resumen del curso

Accesibilidad es transversal a todas las pantallas. En Compose se aprovechan componentes Material que ya aportan semántica, y se agregan metadatos donde hace falta para lectores de pantalla, tamaño táctil y navegación.

## Auditoría del código del Cotejo 11

| Aspecto | Estado anterior | Resultado Cotejo 12 |
|---|---|---|
| `Image(contentDescription)` en el logo | Ya incorporado en Cotejo 02 | Se conserva |
| Etiquetas de campos con Material `OutlinedTextField` | Ya incluido | Se conserva |
| Botones y chips con semántica Material | Incluido por defecto | Se conservan y refuerzan objetivos táctiles |
| Encabezados con `Modifier.semantics { heading() }` | Faltaba | Añadido en autenticación, bienvenida, catálogo y categorías |
| Anuncio de mensajes de error y recuperación | Faltaba | `liveRegion = LiveRegionMode.Polite` |
| Etiqueta accesible específica para mostrar contraseña | Parcial | `contentDescription` contextual desde `strings.xml` |
| Estado expandido/contraído de tarjeta | Faltaba | `stateDescription` localizado |
| Objetivos táctiles 48dp en controles relevantes | Parcial | `heightIn(min = 48.dp)` |
| Pruebas de árbol semántico Compose | Faltaban | Nuevas pruebas instrumentadas (pendientes de ejecutar) |

## Rutas comprobables

- `ui/auth/AuthScreen.kt`: encabezado, errores con `liveRegion` y mensaje durante carga.
- `ui/auth/AuthPasswordField.kt`: botón de visibilidad con acción descriptiva y tamaño táctil.
- `ui/catalog/AnimatedProductDemoCard.kt`: estado semántico de expansión, sin perder animación.
- `ui/catalog/CatalogLearningPreview.kt`: encabezados de catálogo y categorías; chips de tamaño mínimo.
- `ui/components/ThemeModeSelector.kt`: objetivos táctiles de selector del tema.
- `ui/components/FarmAppBrandHeader.kt` y `ui/home/HomeScreen.kt`: encabezados navegables.
- `res/values/strings.xml`: nuevos textos accesibles y traducibles.
- `src/androidTest/.../accessibility/FarmAppAccessibilityTest.kt`: 4 pruebas de semántica.

## Qué enseñar a la profesora

1. Ejecuta la aplicación y activa TalkBack en el emulador (`Settings > Accessibility > TalkBack`).
2. En el formulario, navega por encabezados y comprueba que se anuncia la acción **Mostrar contraseña**.
3. Muestra un error de validación y observa su anuncio accesible.
4. Tras iniciar sesión, recorre las categorías y expande una tarjeta; su estado debe anunciarse como **Detalles expandidos** o **Detalles contraídos**.
5. En Android Studio, abre **Layout Inspector** y muestra el árbol de semántica de la UI.
6. Cambia la escala de fuente y revisa visualmente que los controles no se solapen, en tema claro y oscuro.

## Estado real de verificación

- Se conservaron los archivos previos y se comprobaron referencias de recursos, estructura XML y empaquetado.
- Las **9 comprobaciones de catálogo Kotlin puro** volvieron a aprobarse con `kotlinc` en JVM.
- Los métodos instrumentados se **añadieron**, pero no se han ejecutado: se requiere emulador/dispositivo y Gradle configurado.
- Tampoco se ha hecho una evaluación de accesibilidad completa (TalkBack manual, Accessibility Scanner, contraste, texto grande, navegación con teclado, Switch Access y pruebas reales con usuarios).
- El ZIP es un paquete acumulativo de fuentes para un proyecto Compose, **no un proyecto Gradle autónomo**.

## Documentación de Android

- https://developer.android.com/develop/ui/compose/accessibility/semantics
- https://developer.android.com/develop/ui/compose/accessibility/api-defaults
- https://developer.android.com/develop/ui/compose/accessibility/testing
