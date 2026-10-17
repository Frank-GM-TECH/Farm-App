# Guía — Cotejo 25 Room en Android Studio

## 1) Integración

La entrega es un paquete acumulativo de **fuentes** (`app/src` + informes). No contiene un proyecto Gradle completo.

1. Abre el proyecto base de FarmApp (**Empty Activity**, Kotlin, Compose, `com.example.farmapp`, minSdk 26).
2. Copia o fusiona `app/src/main/...`, `app/src/test/...` y `app/src/androidTest/...` con las carpetas existentes. **No reemplaces tu Gradle y manifiesto sin comparar los archivos actuales**.
3. Comprueba que el manifiesto declara `.FarmAppApplication` y `.MainActivity` (ya incluidos en esta entrega).
4. Sigue `CONFIGURACION_GRADLE_COTEJO_25.md` para instalar las dependencias Room/KSP y sincroniza Gradle.
5. Abre `RoomDemoItem.kt`, `RoomDemoDao.kt`, `RoomDemoDatabase.kt` y `AppContainer.kt` para comprobar la concordancia académica.

## 2) Usar el módulo

1. Ejecuta FarmApp y entra con un usuario Firebase de prueba.
2. En Inicio pulsa **Laboratorio Room local (demo)**.
3. Introduce, por ejemplo, nombre `Gasas demo`, precio `6.50` y cantidad `3`. Pulsa **Guardar en Room**.
4. Comprueba que aparece en la lista. Usa **+1 unidad** y después **Eliminar**.
5. Guarda otro artículo, cierra la app y vuelve a abrirla. Comprueba que el artículo permanece; si desinstalas la app se pierde la base local.
6. El laboratorio SQL del Cotejo 24 continúa en el Inicio de FarmApp y usa una base distinta.

## 3) Database Inspector

1. Ejecuta FarmApp en emulador o dispositivo compatible, con una compilación depurable.
2. Ve a **View → Tool Windows → App Inspection → Database Inspector** (el menú puede variar por versión).
3. Selecciona el proceso `com.example.farmapp`.
4. Localiza `farmapp_room_demo.db` y la tabla `articulos_room_demo`.
5. Ejecuta `SELECT * FROM articulos_room_demo ORDER BY nombre;` desde el inspector si está disponible.
6. Guarda otro artículo en la app y verifica que aparece en la tabla.

## 4) Ejecutar pruebas

En Windows, desde la carpeta raíz de tu proyecto Gradle:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

También puedes usar **Run** sobre `RoomDemoDaoTest`, `RoomDemoViewModelTest`, `RoomDemoFormTest` y `RoomDemoScreenTest`.

### Criterios de aceptación

- Las consultas DAO compilan mediante KSP.
- Insertar, actualizar y eliminar se reflejan en `Flow` y en la interfaz.
- Al reiniciar la app se mantiene la información guardada.
- El Inspector muestra la tabla Room y sus valores.
- El laboratorio SQL del Cotejo 24 permanece funcional.
- Las pruebas locales e instrumentadas pasan realmente en Android Studio.

No marcar estas tareas como verificadas hasta que hayan sido ejecutadas.
