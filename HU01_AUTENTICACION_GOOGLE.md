# FarmApp — HU01: Iniciar sesión con Google + mostrar contraseña

## Lo que cambió

- La app ahora muestra **Continuar con Google** tanto en inicio de sesión como en registro.
- **Credential Manager** muestra el selector oficial de cuenta Google cuando se pulsa el botón.
- `FirebaseAuthRepository.signInWithGoogleIdToken()` canjea el token por una sesión Firebase usando `GoogleAuthProvider`.
- `AuthViewModel` administra carga, éxito, error y cierre de sesión con `StateFlow`.
- Cancelar el selector no muestra un error ni intenta autenticarse; si la configuración Google falta, se muestra un mensaje descriptivo.
- Al salir, Firebase cierra la sesión y Credential Manager limpia su estado almacenado.
- Se mantienen email/password, recuperación y mostrar/ocultar contraseña con `remember`.
- No se incorporan claves de API ni tokens reales al código ni al ZIP.

## Configuración necesaria en tu Firebase (manual)

1. Abre https://console.firebase.google.com/ y crea un proyecto, o utiliza uno propio.
2. Registra una app Android con **applicationId `com.example.farmapp`**. Este valor debe coincidir exactamente con `app/build.gradle.kts`.
3. En Authentication → Sign-in method habilita **Correo electrónico/Contraseña** y **Google**; guarda la configuración.
4. En Android Studio abre la terminal en la raíz FarmApp y ejecuta `./gradlew.bat signingReport` en Windows (tras preparar el wrapper) o usa Gradle → Tasks → android → signingReport. Obtén **SHA-1 y SHA-256 de debug**, de tu computadora, y agrégalas en Firebase → Project settings → Your apps → Android.
5. Descarga otra vez el **google-services.json actualizado** después de habilitar Google y registrar SHA. Copia a `FarmApp/app/google-services.json` con ese nombre exacto. NO lo subas públicamente ni lo envíes a desconocidos.
6. Sincroniza Gradle. El plugin Google Services crea `default_web_client_id`, que usa el botón Google. No copies el ID de cliente Android en lugar del Web client ID.
7. Usa un emulador Android con **Google Play Services** instalado y una cuenta Google disponible, o un dispositivo físico con Play Services. Prueba ambos métodos.
8. Si el botón muestra «Falta configurar…», revisa Google activado en Firebase y vuelve a descargar el JSON. Si da error de desarrollador (`DEVELOPER_ERROR`, error 10), revisa package name, huellas SHA-1/SHA-256 y configuración OAuth. Si la cuenta ya está asociada con otro proveedor, quizá debas realizar la vinculación de cuentas; NO se hace automáticamente en este ejemplo.

## Dónde mostrarlo a tu profesora

- `ui/auth/AuthScreen.kt`: botón Continuar con Google, estados y Mostrar/Ocultar.
- `auth/GoogleCredentialSignIn.kt`: Credential Manager y `GetSignInWithGoogleOption`.
- `auth/FirebaseAuthRepository.kt`: `GoogleAuthProvider` y `signInWithCredential`.
- `auth/AuthViewModel.kt`: StateFlow y comprobaciones del token.
- `FarmAppRoot.kt`: ciclo de vida del selector, manejo de cancelación y limpieza al salir.
- `src/test/.../GoogleAuthViewModelTest.kt`: cinco pruebas locales con fake.
- `src/androidTest/.../AuthGoogleButtonTest.kt`: tres pruebas UI.

## Estado de verificación

La estructura fuente/Gradle y los recursos se han revisado estáticamente. **Este entorno no dispone de Android SDK ni de Firebase de la persona usuaria**: no se ha compilado APK ni probado Google Sign-In real. Las pruebas deben ejecutarse en Android Studio con Firebase configurado.

Comandos para verificar después de sincronizar Gradle:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

El botón requiere configuración OAuth válida en Firebase; instalar o abrir el ZIP no activa por sí solo la autenticación de Google.
