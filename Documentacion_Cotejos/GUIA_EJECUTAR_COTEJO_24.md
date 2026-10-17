# Guía Cotejo 24 — SQL en Android Studio

1. Toma tu proyecto Android Studio Compose existente y fusiona el contenido `app/src` de esta entrega. El ZIP NO incluye gradle wrapper ni build.gradle completos.
2. Conserva los repositorios y dependencias de los 23 cotejos anteriores. El nuevo módulo SQLite usa APIs del SDK Android y `kotlinx-coroutines` existentes; **no requiere añadir Room ni una dependencia SQLite externa**.
3. Asegúrate de que `AndroidManifest.xml` declara `.FarmAppApplication`, tal como en el Cotejo 22.
4. Ejecuta FarmApp, inicia sesión y selecciona «Laboratorio SQL local (demo)» en Inicio.
5. Prueba los chips de consulta y los botones `INSERT`, `UPDATE`, `DELETE`, `Restaurar ejemplos`.
6. En Android Studio, abre `SqlLabSqliteRepository.kt` para explicar los métodos `onCreate`, `rawQuery`, `insertWithOnConflict`, `update`, `delete` y transacciones.
7. Con un emulador iniciado, ejecuta desde la terminal Windows:
   `./gradlew.bat :app:connectedDebugAndroidTest`
8. Verifica el informe en la ventana **Run > Tests** y muestra los resultados a tu profesora.

**Datos de demostración:** la tabla SQL no almacena nombres de pacientes, recetas, compras ni datos médicos reales. La tabla es independiente del catálogo HU03 y de Firebase.
