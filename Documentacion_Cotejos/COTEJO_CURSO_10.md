# Cotejo N.º 10 — Tema Material 3 (Compose)

## Qué pide el curso

MaterialTheme, Theme.kt, Color.kt, LightColorScheme/DarkColorScheme,
tema oscuro, Shape.kt, Type.kt, escala tipográfica y formas.

## Estado anterior (Cotejo 09)

- `ui/theme/Theme.kt`: existía una paleta clara y `MaterialTheme`.
- NO existían `Color.kt`, `Type.kt`, `Shape.kt` ni una paleta oscura.
- Varias superficies usaban `MaterialTheme.typography` pero dependían de la escala predeterminada.
- El Manifest previo no declaraba MainActivity, ni había estilo XML `Theme.FarmApp`.

## Actualización (Cotejo 10)

| Requisito | Evidencia real | Estado |
|---|---|---|
| MaterialTheme | `ui/theme/Theme.kt` + `FarmAppTheme.kt` | Implementado |
| Colores declarados | `ui/theme/Color.kt` | Implementado |
| LightColorScheme | `ui/theme/Theme.kt` + `FarmAppTheme.kt` | Implementado |
| DarkColorScheme | `ui/theme/Theme.kt` + `FarmAppTheme.kt` | Implementado |
| Respeto del modo del sistema | `isSystemInDarkTheme()` | Implementado |
| Selección manual claro/oscuro | `FarmAppRoot.kt`, `ui/components/ThemeModeSelector.kt` | Implementado |
| Escala de 15 estilos | `ui/theme/Type.kt` (Material 3) | Implementado |
| Formas pequeñas/medianas/grandes | `ui/theme/Shape.kt` | Implementado |
| Consumo real de las formas | `FarmAppBrandHeader.kt`, `HomeScreen.kt`, `CatalogLearningPreview.kt`, `PasswordGuidance.kt` | Implementado |
| Recursos de texto | `res/values/strings.xml` | Implementado |
| Regla Compose para pruebas | `androidTest/ui/theme/ThemeModeSelectorTest.kt` | Incluida, pendiente de ejecutar |
| Manifest de arranque | `AndroidManifest.xml`, `res/values/styles.xml` | Corregido; ejecución pendiente |

El selector utiliza **elevación de estado**: `ThemeModeSelector` no guarda el modo;
`FarmAppRoot` lo controla y lo pasa a `FarmAppTheme`.
`rememberSaveable` conserva la selección ante recreaciones ordinarias,
**no** entre cierres completos/reinicios del proceso: persistencia con DataStore queda para una unidad futura.

## Cómo demostrarlo a la profesora

1. `Color.kt`: mostrar las constantes de color.
2. `FarmAppTheme.kt`: observar cómo se elige `lightColorScheme` o `darkColorScheme` y se llama a `MaterialTheme(colorScheme, typography, shapes)`.
3. `Type.kt`: revisar los 15 estilos, tamaños, pesos y fuente.
4. `Shape.kt`: ver `small`, `medium`, `large`.
5. Abrir `ThemeModeSelectorPreview` y alternar entre Claro y Oscuro en modo interactivo.
6. Mostrar `ProductDemoCard` y `FarmAppBrandHeader` consumiendo `MaterialTheme.shapes`.
7. Ejecutar `ThemeModeSelectorTest` en un emulador para mostrar el estado de la prueba.

## Alcance de la validación

Se inspecciona sintácticamente el contenido Kotlin y se verifica que los recursos XML
y referencias internas del tema existan. **No se ha ejecutado Gradle/compilación
completa Android ni pruebas instrumentadas**. Deben validarse en tu Android Studio.
Este ZIP sigue siendo un paquete de código para integrar en una plantilla Compose,
no un proyecto completo listo para abrir con Gradle.

**Organización estándar:** `Theme.kt` contiene `MaterialTheme` y las paletas; `FarmAppTheme.kt` mantiene la función pública para no romper los cotejos anteriores.
