# Cotejo 27 — Guía de ejecución en Android Studio

1. Abre tu proyecto Android Studio `com.example.farmapp` que ya integra los cotejos previos.
2. Copia o fusiona los archivos nuevos/modificados en `app/src` sin borrar dependencias.
3. Agrega `androidx.datastore:datastore-preferences:1.2.1` según `CONFIGURACION_GRADLE_COTEJO_27.md`.
4. Sincroniza Gradle y ejecuta FarmApp en un emulador (con Firebase configurado para acceso).
5. Selecciona **Oscuro** en la barra de tema; pulsa Inicio del emulador y vuelve a abrir
   FarmApp. Confirma que continúa en oscuro.
6. Ve a **Catálogo > Ver galería de imágenes**, espera la carga de productos externos y
   elige **Lista**. Sal de la app y vuelve a abrirla. La galería deberá conservar Lista.
7. Cambia a **Cuadrícula**, cierra y vuelve a abrir: se conserva Cuadrícula.
8. Prueba con el dispositivo rotado; el selector no debe borrar las preferencias.
9. Vuelve al tema **Sistema** y verifica que siga el modo claro/oscuro de Android.

## Pruebas de clase

- `UserPreferencesModelTest.kt`: lectura de preferencias y valores predeterminados.
- `UserPreferencesViewModelTest.kt`: cambios de estado, error de escritura, tests fakes.
- `DataStoreUserPreferencesRepositoryTest.kt`: acceso real al DataStore de prueba.
- `GalleryDataStorePreferenceUiTest.kt`: dos disposiciones de Compose sin HTTP real.

## Cómo demostrarlo a tu profesora

Muestra el archivo `DataStoreUserPreferencesRepository.kt`, señala `preferencesDataStore`,
`Flow`, `catch`, `map`, `edit` y las claves; luego `UserPreferencesViewModel`,
`AppContainer` y `FarmAppRoot`. Ejecuta la app y demuestra persistencia al reiniciar.

## Nota

Una rotación sola no prueba DataStore: `rememberSaveable` también soporta rotación.
**Cierra y reabre** la aplicación para verificar persistencia local real.
La galería utiliza datos ficticios y acceso a Internet; si está sin red, la preferencia
sigue almacenada pero no habrá tarjetas que mostrar hasta que se complete la descarga.
