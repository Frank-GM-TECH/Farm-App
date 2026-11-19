# FarmApp — Rúbrica 9: dashboards verificables

Actualización: 19 de noviembre de 2026. Rama: `main`.

## Acceso en la aplicación

1. Iniciar sesión.
2. Ir a **Cuenta → Paneles de análisis**.
3. Abrir cada uno de los tres paneles: **Inventario**, **Mis pedidos**, **Análisis del catálogo**.
4. El acceso rápido **Cuenta → Mis pedidos** abre directamente el dashboard de pedidos.

La navegación se implementó en `ui/navigation/FarmAppNavHost.kt`, con rutas en
`ui/navigation/FarmAppDestinations.kt`. Todas las rutas verifican que el usuario
esté autenticado.

## Arquitectura y fuentes

| Panel | Datos | Indicadores | Gráficos |
|---|---|---|---|
| Inventario | Instantánea del servidor `products`, sincronizada hacia Room | Productos, unidades, agotados, bajo stock (1 a 5) | Productos y unidades por categoría |
| Mis pedidos | `users/{uid}/orders` desde Firestore `Source.SERVER` | Pedidos válidos, importe acumulado, última compra | Importe y cantidad por mes durante los últimos seis meses |
| Catálogo | Misma instantánea validada de productos, persistida en Room | Productos, con stock, precio medio, categorías | Rangos de precios y precio medio por categoría |

Los paneles comerciales de productos comparten `CatalogDashboardViewModel`.
`CatalogRealtimeSync` ya validaba instantáneas del servidor antes de persistir
productos en Room; ahora publica `VerifiedCatalogSnapshot` **solo después de
una actualización exitosa de Room**. Así los dashboards no presentan las seis
semillas de ejemplo como si provinieran del catálogo real. Cuando todavía no se
verificó el servidor, el panel indica que faltan datos confirmados. Si la
conexión se pierde después de sincronizar, conserva la última instantánea
verificada e informa su fecha, sin afirmar que los valores sigan actualizados.

`OrdersDashboardViewModel` reutiliza el `OrderRepository` existente, que
consulta únicamente la subcolección de pedidos del usuario autenticado y
comprueba el UID de la sesión. No se habilitaron lecturas de pedidos globales,
ni escrituras directas de stock. Los pagos son **simulados**.

`DashboardMetrics.kt` contiene cálculos puros y verificables; las vistas son
Jetpack Compose + Material 3, con tarjetas KPI y barras horizontales construidas
con primitivas de Compose, sin depender de servicios de gráficos externos.

## Explicación técnica para la defensa

- **MVVM:** pantallas Compose observan `StateFlow` del ViewModel con
  `collectAsStateWithLifecycle`. El ViewModel recibe dependencias desde
  `AppContainer`. Las fórmulas se centralizan en `DashboardMetrics`.
- **Corrutinas:** el sincronizador persiste la instantánea Firestore en Room
  mediante una corrutina y un `Mutex`; los pedidos se consultan mediante
  `suspend` y `await`.
- **Inventario:** “bajo stock” significa 1 a 5 unidades, y “agotado” significa
  0. Las cantidades se agrupan por `category`.
- **Catálogo:** precio promedio = suma de precios / número de productos;
  bandas: menos de S/10, S/10–29,99, S/30–59,99, S/60 o más.
- **Pedidos:** importe acumulado = suma de `totalCents` de pedidos
  estructuralmente válidos; la evolución cubre los seis meses que terminan en
  el mes actual, incluso si algún mes tiene cero compras. Fecha a partir de
  `createdAtMillis` en la zona horaria del dispositivo.

## Pruebas en Android Studio

Desde la raíz del proyecto, ejecutar en PowerShell:

```powershell
git status
git pull --ff-only origin main
.\gradlew.bat :app:testNormalDebugUnitTest --tests "com.example.farmapp.dashboard.DashboardMetricsTest"
.\gradlew.bat :app:assembleNormalDebug
.\gradlew.bat :app:assemblePlayStoreDebug
```

Después:

1. Iniciar `normalDebug` y entrar con una cuenta real de demostración.
2. Esperar la sincronización de Firestore; comprobar que Inventario y Catálogo
   muestren fecha de verificación y valores que correspondan a `products`.
3. Cambiar de forma autorizada el stock de un producto de prueba en Firestore
   y observar la actualización de los dos dashboards.
4. Ir a **Cuenta → Mis pedidos** y comprobar importes, meses y última compra
   contra `users/{uid}/orders` en Firebase Console.
5. Probar “Actualizar mis pedidos” y el estado de error sin Internet.
6. Probar la navegación **Volver** y comprobar que el carrito y una compra de
   demostración sigan funcionando.
7. Repetir pruebas de compilación para la variante destinada a Google Play.

## Limitaciones y precauciones

- No se han ejecutado compilaciones Android ni pruebas de teléfono desde el
  conector de GitHub; deben comprobarse en Android Studio.
- `normalDebug` y `playStoreRelease` reutilizan el mismo backend actual;
  no se modificó `functions/src/index.ts`, reglas de Firestore o App Check.
- En una instalación sin conectividad previa verificada, los dashboards del
  catálogo no muestran estadísticas para no confundir datos locales de prueba
  con datos reales.
- Si se va a publicar una nueva versión Play Store, incrementar
  `versionCode` respecto a la versión ya publicada y firmar solo localmente.
- No subir certificados privados, credenciales ni datos de exportación.
