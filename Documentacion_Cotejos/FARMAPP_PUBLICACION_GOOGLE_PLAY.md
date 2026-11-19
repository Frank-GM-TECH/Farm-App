# FarmApp — ruta de publicación en Google Play

## Estado y variantes

- **normalDebug**: paquete `com.example.farmapp`; sigue conectado a Firebase real (`farm-app-c5291`) y conserva App Check Debug para las pruebas existentes.
- **hu04TestDebug**: paquete `com.example.farmapp.hu04test`; mantiene Firebase Emulator.
- **playStoreDebug / playStoreRelease**: variante nueva de publicación, con paquete **provisional** `com.frankgmtech.farmapp`. NO está lista para compilar / distribuir hasta completar su registro Firebase y Play.
- El espacio de nombres Kotlin (`namespace = "com.example.farmapp"`) permanece igual: no obliga a renombrar paquetes Kotlin, clases, vistas ni importaciones.
- Mantener el mismo proyecto Firebase significa conservar Authentication, Firestore (`products`, `users`, pedidos), Storage y Cloud Functions. El carrito Room es local por instalación y no migra automáticamente a una app con otro ID.
- Código de la variante agregado a `app/build.gradle.kts`; no cambia `normalDebug` ni modifica documentos Firestore.

## Bloqueador 1: disponibilidad y registro del paquete

1. En Google Play Console > Crear aplicación, comprobar `com.frankgmtech.farmapp`.
2. **No asumir que está libre**. Si aparece «ya está en uso», NO crear con un identificador distinto sin primero actualizar `applicationId` de la variante Play en GitHub, y mantener la identidad Play + Firebase + AAB coherente.
3. Si está disponible, crear la ficha **sin publicar todavía**. Guardar el nombre del paquete definitivo.

Los nombres de paquete son identificadores duraderos; no se cambian para actualizaciones de una aplicación publicada.

## Bloqueador 2: nueva aplicación Android EN EL MISMO proyecto Firebase

En Firebase Console > proyecto `farm-app-c5291` > Configuración del proyecto > Tus aplicaciones > Agregar Android:

1. Registrar exactamente el ID de paquete aceptado por Google Play.
2. Descargar el nuevo `google-services.json`, correspondiente a ese cliente Android.
3. Guardarlo en `app/src/playStore/google-services.json`, creando la carpeta `playStore` si hace falta. **No sustituir** `app/google-services.json` de `normalDebug`.
4. Asegurarse de que Google Services use el cliente que coincide con el `applicationId` de la variante.
5. Configurar Google Authentication para este nuevo cliente Android (SHA-1 y SHA-256 del certificado debug, carga y firma de Google Play cuando estén disponibles), App Check con Play Integrity y las opciones del canal de distribución.
6. No tocar `functions/src/index.ts`, las reglas ni las colecciones solo por registrar otro cliente.

Si `assemblePlayStoreDebug` o `bundlePlayStoreRelease` muestra «No matching client found for package», suele indicar JSON de otra app o un ID diferente, no pérdida de la base de datos.

## Validación local de los dos canales

Desde la raíz de FarmApp en PowerShell:

```powershell
git pull --ff-only origin main
.\gradlew.bat :app:assembleNormalDebug
```

Cuando se registre la aplicación Firebase de la variante Play y se coloque su JSON:

```powershell
.\gradlew.bat :app:assemblePlayStoreDebug
```

Comprobar el `applicationId`, login, listado de productos y App Check antes de realizar una compra. Los pedidos desde ambos paquetes modificarán la misma colección `products` real: usar un producto con stock conocido y no repetir compras sin investigar errores.

## Publicación y firma

1. Configurar App Check Play Integrity para el cliente Android de distribución.
2. En Android Studio: **Build > Generate Signed Bundle / APK > Android App Bundle**, seleccionar variante `playStoreRelease`.
3. Crear/conservar con seguridad la clave de carga (upload key); **no** incluir keystore ni contraseñas en Git.
4. Al activar Play App Signing, registrar en Firebase el SHA-256 del certificado de firma de aplicación que Google Play exhibe y, cuando proceda para autenticación, su SHA-1.
5. Verificar la aplicación **instalada desde Google Play** (prueba interna o cerrada) antes de endurecer las exigencias `PLAY_RECOGNIZED`/`LICENSED`.
6. Preparar política de privacidad, declaración de seguridad de datos, clasificación de contenidos y proceso de eliminación de cuentas si Firebase Authentication permite crear cuentas.
7. Para cuentas personales nuevas, comprobar los requisitos de prueba cerrada y acceso a producción; una prueba de Play Console no equivale automáticamente a publicación pública.

**Estado deliberado:** `normalDebug` conserva el flujo operativo de demostración; `playStore` solo está preparado a nivel de Gradle y necesita la identidad definitiva y los archivos de Firebase antes de compilarse.

## Referencias oficiales

- https://developer.android.com/build/build-variants
- https://firebase.google.com/docs/android/google-services-plugin-and-file
- https://firebase.google.com/docs/projects/multiprojects
- https://firebase.google.com/docs/app-check/android/play-integrity-provider
- https://support.google.com/googleplay/android-developer/answer/9859152
