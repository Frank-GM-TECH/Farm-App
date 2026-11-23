# FarmApp — Actualización de Play Store 0.2.0 (versionCode 2)

Fecha de preparación: 23 de noviembre de 2026. Rama de referencia: `main`.
Paquete permanente publicado en Google Play: `com.frankgmtech.farmapp`.
**Objetivo: distribuir en PRUEBA INTERNA, no en producción pública.**

## Qué contiene esta actualización

- Tres dashboards navegables desde `Cuenta → Paneles de análisis`:
  **Inventario**, **Mis pedidos** y **Análisis del catálogo**.
- Seis visualizaciones con **Vico 2.3.6**: cinco de columnas y una de
  líneas; datos calculados en `DashboardMetrics` desde productos
  verificados en Firestore/Room o pedidos del usuario autenticado.
- Mejoras de Google Sign-In en `FarmAppRoot.kt` y `AuthViewModel.kt`.
- El usuario confirmó el 19/11/2026 compilación correcta de
  `normalDebug`/`playStoreDebug` y ejecución de los dashboards en Samsung.
- El 23/11/2026 confirmó que Google Sign-In ya funciona en la aplicación
  instalada **desde Google Play** después de agregar las huellas SHA a Firebase.
- Sin cambios al flujo de pagos simulados, Cloud Functions, reglas Firestore
  ni al identificador de paquete publicado.

## Versionado aislado de Google Play

`app/build.gradle.kts`, flavor `playStore`:

```kotlin
create("playStore") {
    dimension = "environment"
    applicationId = "com.frankgmtech.farmapp"
    versionCode = 2
    versionName = "0.2.0"
    targetSdk = 36
    // Resto de configuración existente...
}
```

El `defaultConfig` de `normal` y `hu04Test` conserva la versión `0.1.0`
(`versionCode = 1`). No cambiar el `namespace` de Kotlin.
**Si Play Console ya tiene un AAB con código 2 o superior, subir el
versionCode de `playStore` a un número libre superior ANTES de generar el AAB.**

## Paso 1 — Actualizar y compilar

En PowerShell, desde la carpeta raíz `FarmApp`:

```powershell
git status
git pull --ff-only origin main
.\gradlew.bat :app:testNormalDebugUnitTest --tests "com.example.farmapp.dashboard.DashboardMetricsTest"
.\gradlew.bat :app:assemblePlayStoreDebug
.\gradlew.bat :app:bundlePlayStoreRelease
```

`git pull --ff-only` es seguro si no hay cambios locales rastreados;
no borrar ni añadir carpetas `untracked` sin revisarlas. La tarea
`bundlePlayStoreRelease` permite detectar problemas de compilación,
**pero, como el repositorio no incluye `signingConfig` de release, NO
se debe asumir que ese AAB quede firmado para Play**.
`assemblePlayStoreDebug` NO sustituye a una versión release.

## Paso 2 — Generar AAB firmado desde Android Studio

1. Sin modificar el `applicationId`, sincronizar Gradle y abrir
   **Build → Generate Signed Bundle / APK…**.
2. Elegir **Android App Bundle (AAB)**, no APK.
3. Seleccionar el **mismo archivo keystore de carga**, alias y
   contraseñas que se usaron para la versión anterior. Estos secretos
   permanecen solo en el equipo, nunca en GitHub, mensajes o capturas.
4. Seleccionar **Build Variant = playStoreRelease** y finalizar
   la generación del bundle.
5. Localizar el `.aab` en la ruta indicada por Android Studio.
   Confirmar que corresponde a `com.frankgmtech.farmapp`,
   `versionCode = 2` y versión `0.2.0`.
6. Si Google Play rechaza la firma, revisar la **clave de carga**.
   No usar los certificados `.der` públicos de Play App Signing como
   sustituto de la clave privada de carga.

Referencia: https://developer.android.com/studio/publish/app-signing

## Paso 3 — Publicar SOLO para prueba interna

1. Abrir **Google Play Console** y seleccionar la ficha existente
   de FarmApp (`com.frankgmtech.farmapp`).
2. Ir a **Probar y publicar → Prueba interna** (el texto de menús puede
   variar según la interfaz).
3. Seleccionar **Crear versión** y subir el AAB firmado de 0.2.0.
4. Notas sugeridas para los testers (no prometen funciones no validadas):

   > FarmApp 0.2.0: tres dashboards nuevos de inventario, pedidos y
   > catálogo con gráficos Vico. Mejoras de diagnóstico del acceso con Google.

5. Revisar avisos y pruebas automatizadas de Google Play, guardar
   y habilitar la distribución **en el canal interno** correspondiente.
   No lanzar a producción pública ni a pruebas abiertas sin nueva revisión.
6. En el Samsung inscrito como tester, actualizar **desde Play Store**,
   no mediante instalación directa de `playStoreDebug`.

Referencia:
https://support.google.com/googleplay/android-developer/answer/9859348

## Paso 4 — Verificación del paquete distribuido por Google Play

- Comprobar que Play muestra la actualización y que FarmApp mantiene
  instalada la versión con paquete `com.frankgmtech.farmapp`.
- Probar **Continuar con Google** y también correo y contraseña.
  Las huellas SHA registradas en Firebase ya resolvieron Google Sign-In
  en la versión antigua según la prueba del usuario.
- Abrir los tres dashboards; revisar cifras y gráficos Vico,
  fecha de sincronización y estado sin conexión.
- Comprobar el carrito y efectuar solo **una compra de demostración**
  con un artículo de stock conocido, sin pago real; verificar el pedido
  en `users/{uid}/orders` y el stock en Firestore.
- Confirmar que App Check **Play Integrity** funciona para la instalación
  desde Google Play.
- Anotar resultados antes de promocionar a un canal más amplio.

## Configuración OAuth de Firebase

No hay que forzar una edición de `google-services.json` para resolver una
autenticación que **ya funciona en la app instalada desde Google Play**:
la corrección de certificados OAuth ocurrió en Firebase/Google.

El archivo `app/src/playStore/google-services.json` del repositorio puede
estar desactualizado respecto de las huellas añadidas en Firebase, pero la
app utiliza `default_web_client_id` y el usuario confirmó el acceso en Play.
Para mantener una configuración reproducible, se puede descargar una copia
nueva desde Firebase → Configuración del proyecto → Android
`com.frankgmtech.farmapp` y comparar los clientes OAuth, antes de generar
futuros AAB. No inventar ni modificar clientes OAuth manualmente.

## Estado al registrar este documento

- **Preparado en GitHub:** versión 0.2.0 para flavor `playStore`,
  dashboards Vico y mejoras de Google Sign-In.
- **Pendiente:** nueva compilación de `playStoreRelease` en Android Studio,
  AAB firmado y publicación interna en Play Console.
- **No publicada:** no afirmar que la versión 0.2.0 ya está disponible para
  descargar hasta confirmación de Play Console.
