# Cotejo 01 — Textos componibles (Unidad 1, «Crea un diseño básico»)

**Material usado:** Captura del resumen del codelab «Cómo compilar una app simple con elementos componibles de texto» de Android Developers.

Referencia del codelab: https://developer.android.com/codelabs/basic-android-kotlin-compose-text-composables?hl=es-419

## Revisión sobre el ZIP original de HU01 (14/10/2026)

| Concepto del resumen | ZIP original | ZIP actualizado | Evidencia en código |
|---|---|---|---|
| `@Composable` | Sí | Sí | `ui/auth/AuthScreen.kt`, `ui/home/HomeScreen.kt`, `ui/components/FarmAppBrandHeader.kt` |
| `Text()` | Sí | Sí | `ui/auth/AuthScreen.kt`, `ui/components/FarmAppBrandHeader.kt` |
| `Column()` | Sí | Sí | `ui/auth/AuthScreen.kt`, `ui/components/FarmAppBrandHeader.kt` |
| `Row()` | **No** | **Sí** | `ui/components/FarmAppBrandHeader.kt` |
| `Box()` | **No** | **Sí** | `ui/components/FarmAppBrandHeader.kt` |
| Formato de texto (tamaño/estilo/color) | Sí | Sí | `MaterialTheme.typography`, `fontWeight` y colores en `ui/components/FarmAppBrandHeader.kt` |

## Dónde aparece en FarmApp

- `FarmAppBrandHeader` muestra un ícono provisional con la inicial «F» dentro de `Box`; a la derecha, un `Column` que apila «FarmApp» y «Tu farmacia a un toque». `Row` organiza ambos bloques horizontalmente.
- La pantalla de registro e inicio de sesión (`AuthScreen`) llama a `FarmAppBrandHeader()` y mantiene sus textos existentes en botones, formularios y mensajes.
- La pantalla posterior al inicio de sesión (`HomeScreen`) reutiliza `FarmAppBrandHeader()`.
- Incluye `@Preview` del encabezado para visualizarlo sin iniciar sesión ni configurar Firebase.

## Cómo enseñárselo a la profesora en Android Studio

1. Abre `app/src/main/java/com/example/farmapp/ui/components/FarmAppBrandHeader.kt`.
2. Señala `@Composable` antes de `fun FarmAppBrandHeader`.
3. Señala `Row { }`, el `Box { Text("F") }` y `Column { Text(...); Text(...) }`.
4. Abre la vista **Split/Design** del archivo y actualiza la vista previa (`FarmAppBrandHeaderPreview`).
5. Abre `AuthScreen.kt`, busca `FarmAppBrandHeader(modifier = Modifier.fillMaxWidth())`, ejecuta la app y observa cómo se integra en un caso real.

## Comprobación de funcionamiento

- **Inspección estática realizada:** presentes los cinco elementos del resumen y llamadas al componente desde las pantallas de acceso y bienvenida.
- **Compilación real:** aún no realizada en este entorno: requiere Android Studio, Gradle, Android SDK y la configuración Firebase del usuario.
- **No se modificó la lógica de login:** se mantuvo HU01 (registro, inicio, recuperación de contraseña y cierre de sesión), igual que antes.

## Regla para cotejos futuros

En cada nuevo resumen del curso, anotar: concepto, si estaba implementado, cambios hechos, ruta del código y cómo demostrar el resultado. **No marcar «verificado en ejecución» antes de compilarlo y probarlo en Android Studio.**

## Actualización de evidencia tras el Cotejo 02

Este documento describe el **estado histórico de Cotejo 01**. En la versión actual, el `Box` ya no contiene `Text("F")`: contiene un `Image()` con el logotipo vectorial. Las funciones `@Composable`, `Text()`, `Column()`, `Row()` y `Box()` **siguen presentes**. Para mostrar el código de la versión actual, abre `FarmAppBrandHeader.kt` y consulta `COTEJO_CURSO_02.md`.
