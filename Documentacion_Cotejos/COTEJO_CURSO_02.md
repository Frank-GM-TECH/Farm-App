# Cotejo 02 — Imágenes y recursos en FarmApp

**Lección oficial:** [Cómo agregar imágenes a tu app para Android](https://developer.android.com/codelabs/basic-android-kotlin-compose-add-images?hl=es-419) (Unidad 1, Crea un diseño básico).

## Resultado del cotejo

| Concepto del resumen | Antes (Cotejo 01) | Ahora (Cotejo 02) | Evidencia |
|---|---|---|---|
| Resource Manager / drawable | No había imagen | Agregado | `app/src/main/res/drawable/ic_farmapp_logo.xml` |
| `Image()` | No | Agregado | `ui/components/FarmAppBrandHeader.kt` |
| `painterResource()` | No | Agregado | `ui/components/FarmAppBrandHeader.kt`, `R.drawable.ic_farmapp_logo` |
| `contentDescription` | No | Agregado | `ui/components/FarmAppBrandHeader.kt`, `R.string.cd_farmapp_logo` |
| `strings.xml` | Solo `app_name` | Expandido | `app/src/main/res/values/strings.xml` |
| `stringResource()` | No en el encabezado/formularios | Agregado | `AuthScreen.kt`, `HomeScreen.kt`, `FarmAppBrandHeader.kt` |
| Mensajes de ViewModel localizables | Texto hardcoded | Referencias a `R.string` | `auth/AuthViewModel.kt`, `auth/AuthUiState.kt` |

### Cómo se aplica en FarmApp

- El encabezado de **inicio de sesión, registro y bienvenida** muestra una cruz farmacéutica mediante un `Image` real con un drawable vectorial local.
- Se reemplazó el logo provisional anterior que utilizaba `Text("F")` dentro de un `Box`; `Row`, `Box`, `Column` y `Text` siguen presentes y funcionales, respetando el Cotejo 01.
- Se usa `painterResource(R.drawable.ic_farmapp_logo)` para cargar el archivo vectorial.
- Se usa `contentDescription = stringResource(R.string.cd_farmapp_logo)` para que TalkBack describa esta imagen informativa. En una imagen meramente decorativa, la documentación de Google indica que corresponde `contentDescription = null`.
- Todos los textos estáticos mostrados por los componentes actuales y los mensajes de validación/autenticación se trasladaron a `strings.xml`.
- El mensaje de la sesión usa el parámetro `%1$s` para mostrar el correo sin perder la posibilidad de traducir la frase.
- No se cambiaron las operaciones Firebase Auth del repositorio, ni los contratos del formulario.

### Cómo demostrarlo en Android Studio

1. Crea el proyecto base con paquete `com.example.farmapp` e integra `app/src` del ZIP, tal como indica `README.md`.
2. Abre **View > Tool Windows > Resource Manager** y localiza `ic_farmapp_logo` (Drawable); es un recurso vectorial ya incluido. Si quisieras importar una foto PNG, utiliza **+ > Import Drawables** siguiendo el codelab, pero no es necesario para esta entrega.
3. Abre `ui/components/FarmAppBrandHeader.kt` y señala `Image(...)`, `painterResource(...)`, `contentDescription`, `stringResource(...)`.
4. Abre `app/src/main/res/values/strings.xml` y muestra `cd_farmapp_logo`, `brand_tagline`, `auth_title_sign_in`, `auth_email_label`, etc.
5. Activa **Split / Design** para mostrar la vista `FarmAppBrandHeaderPreview()` sin requerir Firebase.
6. Abre `AuthScreen.kt` y `HomeScreen.kt` y confirma que los textos vienen de recursos, incluidos los mensajes de error del estado.
7. Prueba accesibilidad: activa TalkBack (opcional) y verifica que el símbolo se describa; evalúa si la descripción aporta información útil dado el título adyacente.

### Evidencia verificada y pendientes

- **Verificación estática:** realizada sobre archivos y referencias de recursos, y validación de XML del vector y strings.
- **Compilación de Android:** pendiente; hace falta Android SDK, Gradle, sincronización con Android Studio y Firebase configurado.
- **Prueba de accesibilidad en dispositivo:** pendiente.

**Importante:** Esta entrega contiene archivos `app/src` para integrar con una plantilla Empty Activity Compose, y NO es un proyecto Gradle autocontenido.
