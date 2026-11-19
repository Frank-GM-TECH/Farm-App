# HU04 — Pedidos de demostración en Firebase real

## Alcance

- `normalDebug`: Firebase real (`farm-app-c5291`), pedidos de demostración de usuarios autenticados.
- `hu04TestDebug`: Firebase Emulator Suite; no afecta el inventario de producción.
- Los pagos siguen siendo simulados. Reponer stock manualmente en Firebase Console es válido para las pruebas académicas.
- `createOrder` realiza las validaciones de producto, calcula importes en el servidor y registra pedido + descuento de stock con una transacción atómica.
- La función conserva un `requestId` para impedir el descuento duplicado en reintentos idénticos.

## Seguridad

- El usuario debe tener sesión de Firebase Authentication; no hay lista de UID de compradores.
- En producción, `createOrder` exige Firebase App Check. En Functions Emulator esa exigencia se omite.
- `ALLOW_REAL_ORDERS` está desactivado por defecto: desplegar el código **no** habilita pedidos reales hasta configurar `true` y desplegar de nuevo.
- Las reglas actuales de Firestore deniegan escrituras directas en `products` y en `users/{uid}/orders` desde el cliente. Revisar las reglas vigentes en Firebase Console antes del primer despliegue.
- App Check no sustituye Authentication ni limita automáticamente las compras abusivas.
- El proveedor Debug App Check que utiliza `normalDebug` exige registrar los tokens de depuración de **cada dispositivo de pruebas** en Firebase Console. No compartir ni incluir tokens en Git.

## Preparación local y comprobaciones (PowerShell)

Desde la raíz de FarmApp, con Firebase CLI, Node.js y npm instalados:

```powershell
git pull --ff-only origin main
npm.cmd --prefix functions ci
npm.cmd --prefix functions run build
firebase.cmd use farm-app-c5291
```

Verificar que la compilación termina correctamente. No desplegar si hay errores.

Para la primera publicación, crear **solo en tu máquina** `functions/.env.farm-app-c5291`:

```dotenv
ALLOW_REAL_ORDERS=false
```

El archivo está excluido mediante `functions/.gitignore`. No hacer commits de `.env`.

## Probar `hu04TestDebug`

Arrancar los emuladores correspondientes (Auth, Firestore, Functions y Storage) y efectuar una compra ficticia en `hu04TestDebug`.

Confirmar que:
1. El pedido se registra en el emulador en `users/{uid}/orders/{requestId}`.
2. Se descuenta exactamente la cantidad pedida en `products/{sku}.stock` del **emulador**.
3. Volver a solicitar el mismo `requestId` no descuenta nuevamente.
4. Stock insuficiente produce rechazo sin pedido parcial ni stock negativo.
5. El proyecto Firestore real no ha cambiado.

## Primer despliegue: pedidos reales todavía cerrados

Comprobar que está registrado el token Debug App Check de cada Android `normalDebug` de prueba. Firebase puede necesitar permisos de verificador de App Check para la cuenta de servicio de Functions.

Desde la raíz:

```powershell
firebase.cmd deploy --only functions:createOrder --project farm-app-c5291
firebase.cmd functions:list --project farm-app-c5291
```

Con `ALLOW_REAL_ORDERS=false` se espera rechazo explícito de pedidos reales incluso con sesión válida; la publicación no modifica inventario.

## Activación controlada

Tras revisar Firestore, App Check, costos y el despliegue anterior, cambiar el archivo local a:

```dotenv
ALLOW_REAL_ORDERS=true
```

Desplegar nuevamente **solo** `createOrder`:

```powershell
firebase.cmd deploy --only functions:createOrder --project farm-app-c5291
```

Efectuar **una** compra ficticia de un SKU existente, autenticado en `normalDebug`. Comprobar en Firebase Console que:
- Se creó `users/{uid}/orders/{requestId}`.
- `products/{sku}.stock` se redujo exactamente en la cantidad comprada.
- El catálogo Android actualiza el stock mediante Firestore → Room.
- Los emuladores siguen aislados.

## Desactivación y reposición

Para detener nuevas compras en producción, configurar `ALLOW_REAL_ORDERS=false` y volver a desplegar `createOrder`. Cambiar el archivo local sin redesplegar **no** altera el servicio publicado.

Para reponer existencias de prueba, cambiar manualmente el campo numérico `products/{sku}.stock` en Firestore Console. Reponer stock no borra pedidos ni evita abusos/costos.

## Git y protección

No subir `.env`, tokens de App Check, credenciales de servicio o claves privadas. No usar `firebase deploy` indiscriminadamente: desplegar `functions:createOrder` únicamente. Los cambios de código en GitHub no despliegan automáticamente Cloud Functions salvo que exista un CI explícito para ello.

## Referencias oficiales

- https://firebase.google.com/docs/functions/config-env
- https://firebase.google.com/docs/app-check/cloud-functions
- https://firebase.google.com/docs/app-check/android/debug-provider
