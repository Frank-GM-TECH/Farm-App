# Cotejo 05 — Estado, composición, recomposición y elevación de estado

**Captura de referencia:** el resumen del usuario sobre estado cambiante, composición, composición inicial, recomposición y elevación de estado (Android Basics with Compose).

## Matriz de correspondencia

| Concepto del curso | Estado antes de este cotejo | Evidencia exacta en la versión 05 | Evaluación |
|---|---|---|---|
| Estado: dato que puede cambiar | Implementado | `ui/auth/AuthScreen.kt`: `email`, `password`, `registerMode`, `passwordVisible`; `auth/AuthUiState.kt` para estado de sesión | Cumple en código |
| Composición como árbol de UI | Implementado | `ui/auth/AuthScreen.kt`: `Column` y subcomponentes; `ui/components/FarmAppBrandHeader.kt`: `Row`, `Box`, `Column`, `Text`, `Image` | Cumple en código |
| Composición inicial | Implícito en Compose | `MainActivity.kt` → `FarmAppRoot.kt` → `AuthScreen.kt` (si no hay sesión); las funciones `@Composable` describen la UI inicial | Se deduce de la estructura; observar ejecutando Android Studio |
| Recomposición | Implementado | `AuthScreen.kt`: `remember { mutableStateOf(false) }`, `rememberSaveable { mutableStateOf(…) }`; el hijo visualiza los valores y los cambios producen recomposición | Cumple en código, prueba visual pendiente |
| Elevación de estado (state hoisting) | Parcialmente evidente (ViewModel→UI, campos) | **Nuevo:** `ui/auth/AuthPasswordField.kt` recibe `password`, `isPasswordVisible`, `onPasswordChange`, `onPasswordVisibilityToggle`, sin estado interno. `AuthScreen.kt` conserva el estado y pasa datos/callbacks al hijo. | **Mejorado explícitamente** |

## Archivos relevantes

1. **Modificado:** `app/src/main/java/com/example/farmapp/ui/auth/AuthScreen.kt`. Se reemplaza solo el campo de contraseña en línea por `AuthPasswordField(...)`, con valores y lambdas del componente padre. Permanecen botones, correo, Firebase mediante callbacks, registro, recuperación y asesor de contraseña.
2. **Nuevo:** `app/src/main/java/com/example/farmapp/ui/auth/AuthPasswordField.kt`. Componente sin estado (stateless) con `OutlinedTextField`, texto accesible desde `strings.xml`, `VisualTransformation` y botón Mostrar/Ocultar. Incluye `PasswordFieldHoistingPreview()`, que posee estado solo en la función de ejemplo que lo llama.
3. **Nuevo:** `app/src/androidTest/java/com/example/farmapp/ui/auth/AuthPasswordFieldHoistingTest.kt`. Caso de prueba para escribir texto en el hijo y observar cambio en el padre; alternancia del botón. **No ejecutada** (requiere SDK, Gradle, emulador y Compose UI Test).
4. **Sin cambios:** `app/src/main/java/com/example/farmapp/FarmAppRoot.kt`, `auth/AuthViewModel.kt`, `ui/components/PasswordGuidance.kt`. Mantienen separación de UI y negocio y cálculo del indicador.

## Flujo del estado (a explicar en clase)

```text
AuthScreen (PADRE)
  password by rememberSaveable { mutableStateOf("") }
  passwordVisible by remember { mutableStateOf(false) }
       │
       ▼ pasa valor y visibilidad
AuthPasswordField (HIJO: sin estado interno)
       │
       ▼ onPasswordChange / onPasswordVisibilityToggle
AuthScreen actualiza password / passwordVisible
       │
       ▼ Compose observa cambios y recompone lo necesario
AuthPasswordField muestra valor o nueva transformación visual
```

**Importante:** el proceso de recomposición se desencadena por el estado observable, pero Compose optimiza y puede omitir funciones cuya entrada no cambió; no implica reconstruir toda la Activity ni necesariamente volver a ejecutar todas las funciones componibles.

## Guion para demostrar a la profesora

1. Abrir `AuthPasswordField.kt` y señalar que **no existe** `remember` ni `mutableStateOf()` dentro de `AuthPasswordField`.
2. Señalar los parámetros `password`, `onPasswordChange`, `isPasswordVisible`, `onPasswordVisibilityToggle`; representan estado bajando y eventos subiendo.
3. Abrir `AuthScreen.kt`, identificar quién declara `password` y `passwordVisible` y ver cómo los pasa al hijo.
4. Abrir la Preview `PasswordFieldHoistingPreview` en modo interactivo, cambiar el texto y pulsar Mostrar/Ocultar. También puede usarse `AuthScreenInteractivePreview` y alternar Registro para ver el asesor.
5. Explicar que `FarmAppRoot.kt` observa con `collectAsStateWithLifecycle` el estado de `AuthViewModel`, usado para decidir entre pantalla de acceso y bienvenida.
6. Si está preparado el emulador, ejecutar `AuthPasswordFieldHoistingTest.kt` y `AuthScreenStateTest.kt`. No afirmar que pasaron antes de ejecutarlos.

## Próximos sprints

- **HU02:** `ProfileScreen` (padre + ViewModel) eleva el estado de `ProfileForm` (hijo), y los datos guardados se validan en repositorio.
- **HU03:** `CatalogScreen` ofrece estados de búsqueda, categoría y filtro a componentes sin estado; ViewModel combina la consulta con el catálogo.
- **HU04:** `CartScreen` expone cantidades y subtotales; se actualizan mediante eventos y el repositorio local persiste datos.
- **HU06:** el inventario expone estados de carga/alerta/error y evita guardar datos de negocio únicamente en un Composable.

## Qué se validó y qué no

- Se verificó con inspección estática que el hijo recibe los valores, expone eventos y no declara estado.
- Los recursos XML permanecen válidos.
- Las pruebas JVM puras heredadas para validadores pueden ejecutarse independientemente de Android (resultado en registro de verificación incluido).
- **No se ejecutaron** una compilación Gradle Android completa, una prueba de UI instrumentada o Firebase.
- El ZIP es un paquete de archivos fuente para incorporar sobre una plantilla Android Studio Compose, no un proyecto Gradle completo autónomo.
