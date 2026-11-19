# HU04 — Migración gradual de App Check Debug a Play Integrity

## Estado de la migración

El carrito y los pedidos de demostración con `normalDebug` ya fueron probados por el equipo en Firebase real; el backend `createOrder` exige App Check. Este documento describe la migración del proveedor Android para permitir el uso en otros teléfonos sin registrar un **token de depuración por dispositivo**.

La integración está preparada en el código, **pero no se activa automáticamente**: la opción `USE_PLAY_INTEGRITY_APP_CHECK` del flavor `normal` permanece inicialmente en `false`, para conservar el checkout operativo hasta que se complete la configuración en Firebase.

## Selección de proveedor

| Variante | Proveedor actual | Objetivo |
| --- | --- | --- |
| normalDebug | Debug mientras el indicador sea false | Play Integrity cuando se active |
| normalRelease | Play Integrity | Play Integrity |
| hu04TestDebug | Debug | Conservar compatibilidad con Emulator Suite |

El proveedor se instala al iniciar `FarmAppApplication`, antes de crear el contenedor de repositorios. El módulo `app` incluye `firebase-appcheck-playintegrity` bajo el Firebase BoM.

## Configuración externa necesaria — Firebase / Play Console

1. Usar el proyecto existente `farm-app-c5291` y la aplicación Android `com.example.farmapp`.
2. Abrir Google Play Console, seleccionar o agregar la aplicación e ir a **App integrity** para **vincular la API Play Integrity** al mismo proyecto de Google Cloud que usa Firebase. La consola y la titularidad del proyecto pueden requerir permisos adicionales o una cuenta de desarrollador.
3. En Firebase Console > App Check > Apps, registrar o configurar `com.example.farmapp` con **Play Integrity**.
4. Obtener desde Android Studio: `./gradlew :app:signingReport` (PowerShell: `.\gradlew.bat :app:signingReport`) y registrar el SHA-256 del certificado con el que se firma la APK que instalarán los usuarios. Si distintos equipos compilan APK con diferentes certificados debug, sus SHA-256 son distintos.
5. Para una APK distribuida *fuera de Google Play*, configurar en los ajustes avanzados de App Check:
   - **PLAY_RECOGNIZED: Not required / No obligatorio**.
   - **LICENSED: Not required / No obligatorio**.
   - **Minimum device integrity: Device integrity / Integridad del dispositivo**.
6. Conservar los tokens de depuración ya registrados mientras se comprueba el cambio; no publicarlos ni compartirlos.

**Atención:** La configuración predeterminada puede exigir `PLAY_RECOGNIZED`, que una aplicación no publicada en Google Play no recibe. La configuración avanzada para distribución externa es indispensable. Play Integrity puede rechazar dispositivos sin la integridad necesaria. No garantiza compatibilidad universal.

## Activación en GitHub (después de configurar Firebase)

En `app/build.gradle.kts`, dentro del flavor `normal`, cambiar:

```kotlin
buildConfigField("boolean", "USE_PLAY_INTEGRITY_APP_CHECK", "false")
```

por:

```kotlin
buildConfigField("boolean", "USE_PLAY_INTEGRITY_APP_CHECK", "true")
```

No modificar `hu04Test`. Registrar el cambio como commit en `main`, hacer `git pull --ff-only origin main` en la computadora, sincronizar Gradle y compilar `normalDebug`.

## Validación en Android Studio y Firebase

- En Logcat, filtrar `FarmAppAppCheck`. Debe mostrarse `Proveedor Play Integrity instalado`.
- Iniciar sesión con un usuario de Firebase Authentication real.
- Consultar productos activos.
- Hacer **una** compra de demostración y comprobar el nuevo pedido en `users/{uid}/orders/{requestId}` y el stock reducido en `products/{sku}.stock`.
- Comprobar que la sincronización de Room refleje el cambio.
- Instalar la **misma APK firmada** en un segundo dispositivo Android compatible y repetir la prueba sin registrar ningún token de depuración manual.
- Comprobar métricas de App Check y errores de Cloud Functions. Si App Check deniega la solicitud, confirmar SHA-256, vinculación del proyecto, los ajustes de distribución externa y la integridad del dispositivo **antes de repetir la compra**.

Para volver de forma temporal al proveedor Debug en `normalDebug`, poner `false`, registrar el cambio en Git y recompilar la aplicación. No es necesario redesplegar `createOrder` para cambiar el proveedor Android.

## Referencias oficiales

- https://firebase.google.com/docs/app-check/android/play-integrity-provider
- https://firebase.google.com/docs/app-check/android/debug-provider
- https://firebase.google.com/docs/app-check/cloud-functions
