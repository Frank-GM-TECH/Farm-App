# Cotejo 04 — Composiciones, botones y estado observable (Unidad 2 del curso)

## Fuente de la evaluación
Captura aportada por el usuario: funciones `@Composable`, composiciones, `Button`, recurso `drawable`, `Image`, UI interactiva, `remember` y `mutableStateOf`.

## Matriz de trazabilidad (acumulativa)

| Concepto exigido | Estado al inicio del cotejo | Evidencia concreta en versión 04 | Qué observa la profesora |
|---|---|---|---|
| Define funciones componibles | Incluido | `ui/auth/AuthScreen.kt`: `@Composable fun AuthScreen(...)`; `ui/components/FarmAppBrandHeader.kt` | Funciones Compose reutilizables |
| Diseños con composición | Incluido | `ui/auth/AuthScreen.kt`: `Column`, campos y botones; `FarmAppBrandHeader.kt`: `Row`, `Box`, `Column` | Composición de pantallas mediante componentes |
| Botón `Button` | Incluido | `AuthScreen.kt`: botón `Registrarme` / `Ingresar`; `ui/home/HomeScreen.kt`: `Cerrar sesión` | Eventos `onClick` reales asociados a callbacks |
| Drawable importado | Incluido desde Cotejo 02 | `res/drawable/ic_farmapp_logo.xml` | Icono del proyecto en Resource Manager |
| Imagen `Image` | Incluido desde Cotejo 02 | `FarmAppBrandHeader.kt`: `Image(painter = painterResource(...), contentDescription = stringResource(...))` | Logo mostrado de forma accesible |
| UI interactiva | Incluida parcialmente | `AuthScreen.kt`: edición correo/contraseña, modo registro/ingreso, nuevo botón Mostrar/Ocultar | Los clics actualizan la UI sin recrear toda la Activity |
| `remember` | Incluido en `PasswordGuidance.kt` para recordar el asesor y su resultado | **Nuevo ejemplo directo** `AuthScreen.kt`: `var passwordVisible by remember { mutableStateOf(false) }` | El estado de visibilidad permanece entre recomposiciones |
| `mutableStateOf()` | Incluido en `AuthScreen.kt` a través de `rememberSaveable` | **Nuevo ejemplo explícito** de `remember { mutableStateOf(false) }` en `AuthScreen.kt` | Cuando se modifica `passwordVisible`, cambia `visualTransformation` y el texto del botón |

## Qué se añadió/modificó en este cotejo
- `AuthScreen.kt`: estado booleano efímero `passwordVisible` almacenado con `remember { mutableStateOf(false) }`.
- `AuthScreen.kt`: control `TextButton` dentro del campo Contraseña; al pulsar *Mostrar* se visualiza la contraseña y el control pasa a *Ocultar*. En el siguiente clic se vuelve a enmascarar. El botón de ingreso/registro original continúa usando `Button`.
- `AuthScreen.kt`: `@Preview` interactiva para probar los cambios sin Firebase.
- `res/values/strings.xml`: textos localizables `auth_show_password`, `auth_hide_password`.
- `app/src/androidTest/.../AuthScreenStateTest.kt`: **propuesta** de dos pruebas instrumentadas de recomposición (alternancia de visibilidad y cambio a registro). Deben ejecutarse en Android Studio; no se ejecutaron en este entorno.

## Por qué aquí se usan `remember` y `rememberSaveable`
- `remember { mutableStateOf(false) }` guarda temporalmente si se muestra la contraseña mientras la pantalla siga componiéndose. Se restablece a *oculta* si Android recrea la Activity, elección conservadora para seguridad.
- `rememberSaveable { mutableStateOf("") }` se mantiene en correo y contraseña: además de las recomposiciones, preserva el contenido durante cambios de configuración admitidos. `registerMode` también permanece en `rememberSaveable`.
- El estado de carga y autenticación **no** vive en `remember`, sino en `AuthViewModel` (`StateFlow`), porque pertenece a la lógica de negocio.

## Cómo demostrarlo en clase
1. Abre `FarmAppBrandHeader.kt` y señala `@Composable`, `Row`, `Box`, `Column`, `Image`, `painterResource`.
2. Abre `AuthScreen.kt` y señala `Button(onClick = ...)` junto con el nuevo `var passwordVisible by remember { mutableStateOf(false) }`.
3. En Android Studio usa `Split` o `Design`, abre `AuthScreenInteractivePreview` y activa **Interactive mode**.
4. Escribe una contraseña, pulsa **Mostrar** y **Ocultar**. El texto visible cambia sin lanzar otra pantalla.
5. Pulsa **¿No tienes cuenta? Regístrate** y verifica que cambia el formulario y aparece el asesor de contraseña implementado en Cotejo 03.
6. Señala los recursos en `strings.xml` y el logo en Resource Manager.

## Pruebas instrumentadas pendientes
En el `dependencies` de `app/build.gradle.kts` deben estar (Compose BOM ya configurado en la plantilla):

```kotlin
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
debugImplementation("androidx.compose.ui:ui-test-manifest")
```

Además, usa el `testInstrumentationRunner` de AndroidX del proyecto. Abre `AuthScreenStateTest.kt` y ejecútalo en emulador o móvil (Run 'AuthScreenStateTest'). Las dos pruebas incluidas **no** se han ejecutado aquí; no se garantiza resultado hasta correr Android Studio.

## Aplicación en futuros sprints
- HU02: botones para editar perfil y acciones guardadas como estado de pantalla.
- HU03: selección de categoría, búsqueda, filtros y detalle de medicamentos con estado observable.
- HU04: cantidad de productos, subtotal y controles `+` / `−`; el carrito real será gestionado por ViewModel/Room para no perder datos.
- HU06: elementos visuales de alertas de inventario, con estados administrados por ViewModel.

## Verificación honesta
Se verifican sintácticamente las referencias en archivos fuente, los recursos XML y la estructura del ZIP. **No se ejecutó Gradle, Android Studio, Firebase ni las pruebas UI instrumentadas**. Este paquete, como el anterior, contiene fuentes que se copian sobre un proyecto Empty Activity (Compose), no un proyecto Android completo ejecutable sin integración.
