# FarmApp — Proyecto Gradle preparado desde Cotejo 32

## Qué contiene

Este ZIP reúne **un solo módulo Android `app/`** con los archivos acumulados de los 32 cotejos, y agrega `settings.gradle.kts`, archivos de compilación, lista consolidada de dependencias, preferencias Gradle, guía e instrucciones para obtener el wrapper oficial.

**Estado real: preparado para importación, NO compilado ni certificado.** En el entorno de preparación no estaban instalados el Android SDK ni Gradle ni había acceso a los repositorios para descargar dependencias. Es necesario sincronizar, corregir posibles errores y ejecutar pruebas en un Android Studio instalado.

## Primeros pasos (Windows)

1. Instala Android Studio actualizado y el SDK de Android **API 36**, y selecciona JDK 17 (Embedded JDK sirve).
2. Extrae el ZIP a una carpeta, por ejemplo `C:\Proyectos\FarmApp`.
3. Ejecuta en PowerShell desde la carpeta extraída: `powershell -ExecutionPolicy Bypass -File .\preparar-gradle.ps1` para descargar Gradle y generar el wrapper OFICIAL. Necesitas Internet. Alternativa: con Gradle 8.13 instalado, usa `gradle wrapper --gradle-version 8.13`.
4. Abre Android Studio > **File > Open** y selecciona la carpeta raíz `FarmApp` (la que contiene `settings.gradle.kts`).
5. Pulsa **Sync Project with Gradle Files**. Espera la descarga de dependencias. Si hay errores, conserva el panel Build completo para solucionarlos.
6. Ejecuta `./gradlew.bat :app:assembleDebug` (Windows) o pulsa **Build > Make Project**.
7. Crea un emulador Android, añade Firebase si vas a probar HU01 y ejecuta la app.
8. Pruebas locales: `./gradlew.bat :app:testDebugUnitTest`.
9. Pruebas instrumentadas: `./gradlew.bat :app:connectedDebugAndroidTest` con un emulador activo.

## Firebase HU01 (obligatorio para iniciar sesión)

- Registra una app Android en tu proyecto Firebase con el ID `com.example.farmapp`.
- Descarga el archivo **`google-services.json`** desde la configuración de esa app y colócalo tú mismo en `app/google-services.json`.
- En Firebase Authentication habilita el proveedor correo/contraseña.
- No compartas contraseñas, cuentas de servicio, ni archivos privados. `google-services.json` está excluido de Git en este paquete.
- El plugin `google-services` se activa de forma condicional si ese archivo existe. Sin esa configuración, la aplicación puede compilar, pero el inicio de sesión Firebase **no está operativo**.

## Versión de herramientas seleccionada

- Android Gradle Plugin **8.13.2**, Gradle **8.13**, Kotlin **2.2.21**, plugin Compose **2.2.21** y KSP **2.2.21-2.0.5**.
- Compila con **SDK 36** (no usa Compose 1.12, que requiere SDK 37); BOM Compose **2026.06.00**.
- minSdk 26, targetSdk 35, JDK 17.

## Notas de integración

- Se corrigió un import inexistente de `rememberSaveable` en `LegacyRoomEditDialog.kt`.
- El resto de código Kotlin se preservó deliberadamente para no perder cotejos. **No se afirma compilación exitosa**; pueden existir incompatibilidades de código fuente o dependencias que solo detectará Gradle.
- El laboratorio SQL, Room, DataStore y WorkManager contiene solo datos educativos, no inventario real.
- Las guías anteriores se conservan en `Documentacion_Cotejos/`.

## Diagnóstico rápido

Si Android Studio muestra un error, copia las primeras líneas de **Build Output**. No borres código ni versiones sin identificar primero la causa. Priorizar la compilación de HU01 antes de ampliar otros sprints.

## NUEVO: HU01 con Google

Se añadió **Continuar con Google**, manteniendo el formulario tradicional y Mostrar/Ocultar. Antes de probarlo lee `HU01_AUTENTICACION_GOOGLE.md`: requiere registrar SHA-1/SHA-256, habilitar el proveedor Google y descargar el `google-services.json` actualizado.
