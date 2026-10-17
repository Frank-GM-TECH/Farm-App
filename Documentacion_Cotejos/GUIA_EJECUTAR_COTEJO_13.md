# Guía: Cotejo 13 — Ciclo de vida de la actividad

## 1. Instalar

1. Abre el proyecto FarmApp creado desde **Empty Activity** en Android Studio.
2. Integra el contenido de `app/src` de este ZIP, conservando los archivos Gradle de tu proyecto.
3. Comprueba que el `AndroidManifest.xml` tiene `MainActivity` como Activity del launcher y que no incluye `android:configChanges="orientation|screenSize"` (interceptar manualmente estos cambios impediría el ejercicio de recreación normal).
4. Configura Firebase cuando quieras probar autenticación real. Los dos tests de Compose nuevos usan callbacks simulados.

## 2. Ver en Logcat

1. Compila en **debug** y ejecuta la app en emulador/celular.
2. Abre **Logcat**, elige el proceso `com.example.farmapp` y filtra por etiqueta `FarmAppLifecycle` (en la interfaz de Logcat reciente puedes usar `tag:FarmAppLifecycle`).
3. Abre FarmApp, pulsa el botón Inicio del emulador, vuelve a la app y observa la secuencia.
4. Gira de vertical a horizontal si tu emulador lo permite (habilita la autorrotación y verifica que Activity se recree).
5. Anota las líneas que observes y toma una captura de pantalla para la profesora.

## 3. Comprobar rememberSaveable

1. En formulario de autenticación, escribe un **correo ficticio** (no uses una cuenta real para una demostración).
2. Cambia al formulario **Crear cuenta**, elige **Oscuro** en el selector de tema y gira el teléfono.
3. Deben mantenerse el correo, modo registro y tema. La contraseña **no** debe persistir tras la recreación.
4. En catálogo demo (si tienes una sesión de prueba), repite con búsqueda y filtro.

## 4. Pruebas automatizadas

Desde la terminal de Android Studio (Windows):

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

La clase `LifecycleStateRestorationTest.kt` utiliza `StateRestorationTester`, que simula el guardado/restauración de Compose; **no es una rotación física de Activity**. Para el comportamiento global de `MainActivity`, haz la comprobación de Logcat con el emulador.

**Estado actual:** pruebas instrumentadas creadas pero no ejecutadas en este entorno. Si Android Studio te muestra un error de dependencias de `ui-test-junit4`, verifica las dependencias de Compose de la guía previa.
