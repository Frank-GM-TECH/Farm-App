# Cotejo N.º 13 — Ciclo de vida de Activity, Logcat y cambios de configuración

## Material cotejado

Las dos capturas aportadas corresponden a **Ciclo de vida de la actividad**, **Registra con Log** y **Cambios de configuración** del curso Android Basics with Compose.

## Estado previo comprobado en Cotejo 12

| Concepto | Antes | Después |
|---|---|---|
| `onCreate()` | Ya existía | Se conservó + registro |
| `onStart()` | No sobrescrito | Se añadió |
| `onResume()` | No sobrescrito | Se añadió |
| `onPause()` | No sobrescrito | Se añadió |
| `onStop()` | No sobrescrito | Se añadió |
| `onRestart()` | No sobrescrito | Se añadió |
| `onDestroy()` | No sobrescrito | Se añadió |
| `Log.d()` | No en MainActivity | Se añadió, etiqueta `FarmAppLifecycle` |
| `rememberSaveable` | Ya en correo, password, modo, tema y catálogo | Se conserva para estado **no sensible**; la contraseña cambia a `remember` por privacidad |
| Pruebas de restauración | Sin caso dedicado | Dos tests instrumentados con `StateRestorationTester` |

## Ubicación del código

- `app/src/main/java/com/example/farmapp/MainActivity.kt`: los siete métodos de ciclo de vida, registros de depuración `Log.d(TAG, ...)`, señal de `savedInstanceState` e indicación de cambio de configuración con `isChangingConfigurations`.
- `app/src/main/java/com/example/farmapp/ui/auth/AuthScreen.kt`: `email` y `registerMode` persisten tras una recreación, pero la contraseña se limpia por seguridad.
- `app/src/main/java/com/example/farmapp/FarmAppRoot.kt`: se mantiene `selectedThemeName` con `rememberSaveable`.
- `app/src/main/java/com/example/farmapp/ui/catalog/CatalogLearningPreview.kt`: se conserva búsqueda y filtro con `rememberSaveable`.
- `app/src/androidTest/java/com/example/farmapp/ui/lifecycle/LifecycleStateRestorationTest.kt`: verifica estado que persiste y contraseña que no persiste.

## Cómo demostrar el ciclo de vida

Filtra Logcat de Android Studio por `FarmAppLifecycle`. Ejemplos frecuentes:

- Abrir la app: `onCreate` → `onStart` → `onResume`.
- Ir a Inicio del teléfono: `onPause` → `onStop`.
- Regresar a FarmApp: `onRestart` → `onStart` → `onResume`.
- Girar un dispositivo con rotación habilitada y recreación de Activity: `onPause` → `onStop` → `onDestroy`, luego `onCreate` → `onStart` → `onResume`.

**Advertencia:** no todos los callbacks se garantizan en toda circunstancia. Por ejemplo, cuando el proceso es finalizado por Android, `onDestroy()` puede no ejecutarse. Los logs solo se muestran en compilaciones depurables, y nunca incluyen contraseñas/correos.

## `remember` vs `rememberSaveable`

- `remember` resiste recomposiciones mientras el componente sigue en composición, pero no la recreación completa de la Activity.
- `rememberSaveable` puede restaurar valores compatibles tras recrear la Activity, usando el mecanismo de estado guardado del sistema. No es persistencia duradera ni reemplaza DataStore/Room.
- `ViewModel` suele sobrevivir a cambios de configuración, pero **no equivale** a la restauración de estado tras muerte del proceso. Para ese escenario se utiliza `SavedStateHandle` cuando corresponda.

## Verificaciones y límites

- Se revisó que los callbacks sobrescritos llamen a `super` y que la etiqueta `FarmAppLifecycle` sea consistente.
- Se revisó que el ZIP conserve todo lo anterior y que los nuevos tests tengan anotación `@Test`.
- La compilación Android completa, pruebas instrumentadas en emulador y prueba de rotación real **quedan pendientes** por falta de SDK Android/Gradle configurados en este entorno.
- El archivo sigue siendo **un paquete de archivos fuente**, NO un proyecto autónomo importable como proyecto Gradle.
