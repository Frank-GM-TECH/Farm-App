# FarmApp — diagnóstico de Google Sign-In distribuido por Google Play

Fecha: 19 de noviembre de 2026. Proyecto Firebase: `farm-app-c5291`.
Aplicación de distribución: `com.frankgmtech.farmapp`.

## Estado verificado

- `normalDebug` (`com.example.farmapp`) ha autenticado con Google en el Samsung.
- La versión `playStoreRelease` distribuida por prueba interna de Google Play
  permite autenticarse por correo, pero se informó un fallo al usar Google.
- Los dashboards con Vico funcionan en Samsung y las variantes
  `normalDebug`/`playStoreDebug` compilaron; el despliegue Play de los nuevos
  cambios aún está pendiente.
- Las aplicaciones pertenecen al mismo proyecto Firebase, pero Google Sign-In
  verifica la **combinación de paquete Android y certificado de firma SHA-1**.

## Hallazgo del repositorio: revisar la huella de Play App Signing

Al inspeccionar `app/src/playStore/google-services.json` en `main`:

- Existe el cliente Android `com.frankgmtech.farmapp`.
- Este cliente contiene **un** OAuth Android (`client_type = 1`).
- Su única huella SHA-1 coincide con una de las dos registradas para
  `com.example.farmapp` (`normalDebug`).
- Los dos clientes usan un OAuth web `client_type = 3` con el mismo client ID,
  lo cual es coherente con el mismo proyecto Firebase.
- **No podemos confirmar desde GitHub** que la única SHA-1 de Play Store sea
  realmente el certificado de firma administrado por Google Play.

Este es el **principal punto a verificar**. El certificado de subida del AAB
(`upload key`) **no es** necesariamente el certificado con el que Google firma
las APK que instala el usuario. No reemplazar la huella real por una suposición.

### Comprobación en consolas (sin publicar una nueva versión todavía)

1. **Play Console** → FarmApp → **Integridad de la aplicación** (App integrity)
   → **Firma de aplicaciones de Play** → **Certificado de firma de la aplicación**
   (`App signing key certificate`). Copiar **SHA-1 y SHA-256** de ese certificado.
   **No** usar solo el certificado de la *clave de carga* (`Upload key certificate`).
2. **Firebase Console** → proyecto `farm-app-c5291` → ⚙️ Configuración del
   proyecto → Tus apps → seleccionar exactamente la app Android
   `com.frankgmtech.farmapp` → Huellas digitales del certificado.
3. Confirmar que figuran la SHA-1 y SHA-256 **del certificado App signing**
   obtenido del paso 1. Si faltan, añadirlas a esa app Firebase y guardar.
   No borrar las huellas existentes; otras pueden corresponder a pruebas o carga.
4. En **Firebase Authentication → Sign-in method**, verificar que el proveedor
   **Google** sigue habilitado. Puesto que funciona en `normalDebug` con el
   mismo proyecto, esto ya es probable.
5. En Firebase, descargar un `google-services.json` actualizado y asegurarse
   de que contiene el cliente para `com.frankgmtech.farmapp` y el OAuth web.
   Reemplazar **solo** `app/src/playStore/google-services.json` en GitHub
   (con commit individual). No reemplazar `normal` ni compartir credenciales
   privadas. El JSON de Firebase no es una clave privada, pero se revisará
   igualmente antes de registrarlo.
6. Tras guardar las huellas en Firebase/Google Cloud, **primero probar de nuevo
   el botón Google en la versión ya instalada desde Play**. La configuración
   OAuth puede corregirse del lado de Google sin cambiar el APK, si el client ID
   web ya está correcto. Si no funciona, necesitamos el código exacto del error
   y, cuando corresponda, generar un nuevo AAB con el JSON actualizado.

Guías oficiales:
- https://firebase.google.com/docs/auth/android/google-signin
- https://firebase.google.com/docs/android/troubleshooting-faq
- https://developer.android.com/studio/publish/app-signing
- https://developers.google.com/android/guides/client-auth

## Ajustes de código registrados en `main`

- `FarmAppRoot.kt`: no limpia `CredentialManager` antes de cada intento de
  inicio de sesión. `clearCredentialState()` se conserva en **cerrar sesión**,
  como recomienda Android.
- `FarmAppRoot.kt`: distingue errores de `GetCredentialException` y registra
  solo tipo y clase del error, sin volcar ID tokens ni detalles privados.
- `AuthViewModel.kt`: distingue fallos de Firebase Auth y escribe su
  `FirebaseAuthException.errorCode` o el nombre de la excepción.
- No se alteró `FirebaseAuthRepository.signInWithGoogleIdToken`, que continúa
  usando `GoogleAuthProvider.getCredential(idToken, null)` y `await()`.

**Estos cambios todavía no están presentes en la app antigua de Play Store**.
Se incorporarán al siguiente AAB, después de corregir la configuración OAuth.
No confundir una corrección registrada en GitHub con una corrección publicada.

## Si aún falla: localizar el paso preciso

Con el teléfono conectado por USB y la versión de prueba instalada desde Play:

1. Abrir Android Studio → **Logcat**, seleccionar el dispositivo y el proceso
   `com.frankgmtech.farmapp`.
2. Filtrar por `FarmAppGoogleSignIn` o `FarmAppGoogleAuth`.
3. Pulsar `Iniciar con Google`. Si se ve `GetCredentialException`, el fallo
   ocurre **antes de entregar el token** a Firebase: verificar paquete, SHA-1,
   OAuth Android y `default_web_client_id`.
4. Si se ve `FirebaseAuthException`, el token se entregó pero Firebase no
   completó la sesión: verificar el código `ERROR_...`, el proyecto,
   proveedor Google, conectividad y configuración del token.
5. Compartir únicamente los nombres o códigos de error y el tramo necesario
   del registro. **No compartir ID tokens, contraseñas, API keys privadas ni
   exportaciones completas de usuarios**.

Para actualizar el código local con los ajustes, cuando `git status` no muestre
modificaciones rastreadas pendientes:

```powershell
git pull --ff-only origin main
.\gradlew.bat :app:assembleNormalDebug
.\gradlew.bat :app:assemblePlayStoreDebug
```

**Advertencia:** `playStoreDebug` puede compilar correctamente, pero se
firma con el certificado de depuración local, no con el certificado final de
Google Play. Por tanto, su Google Sign-In no verifica por sí solo la
configuración OAuth de la aplicación distribuida.

## Publicación de dashboards + Google Sign-In

Tras verificar Google Sign-In en una instalación de Play:

1. Incrementar `versionCode` (la versión publicada es `1`; usar `2` o
   superior), sin cambiar el `applicationId`.
2. Compilar `playStoreRelease` y generar un AAB **firmado localmente con la
   clave de carga protegida**, fuera del repositorio.
3. Subir a **Prueba interna**; instalar la actualización desde Google Play.
4. Probar Google Sign-In, correo, catálogo, los tres dashboards y **una compra
   de demostración controlada**; comprobar stock en Firestore.
5. Solo después decidir si ampliar la distribución o continuar con la rúbrica.

No modificar la Cloud Function de compras, los permisos Firestore, App Check ni
la firma únicamente por un error OAuth sin pruebas de su causa.
