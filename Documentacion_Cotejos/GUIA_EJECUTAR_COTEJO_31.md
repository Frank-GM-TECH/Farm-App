# Guía de Cotejo 31 — Compose en una pantalla Views

1. Parte de la entrega acumulativa `FarmApp_Cotejo31_Actualizado/app/src` e intégrala en tu proyecto Android Studio `com.example.farmapp` de tipo Compose, con la configuración Gradle de cotejos anteriores.
2. Confirma que `buildFeatures { compose = true; viewBinding = true }` está configurado, junto con Material 3/Compose BOM, RecyclerView, Fragment, Room, Navigation Fragment y las demás dependencias documentadas.
3. Ejecuta el proyecto. Desde Inicio abre **Inventario con objetos View (demo)**. El contenedor sigue siendo un Fragment con RecyclerView y FAB, pero las filas se ven como tarjetas Material 3 de Compose.
4. Presiona `+`, agrega un artículo de práctica y toca una tarjeta: el mismo `DialogFragment` de Views debe abrirlo para editar.
5. Si hay una modificación de existencias en Room desde otra pantalla, el `RecyclerView` debe actualizar la fila Compose.
6. Abre `LegacyInventoryComposeRow.kt` y ejecuta la preview `LegacyInventoryComposeRowPreview()`; no requiere iniciar sesión ni conectar Firebase.
7. Ejecuta la prueba instrumental `LegacyInventoryComposeRowTest` en un emulador, usando el menú contextual `Run` de Android Studio.
8. Para todo el conjunto, usa en la terminal Windows:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

**Atención:** el ZIP no incluye archivos `build.gradle.kts` ni el wrapper `gradlew`; esos comandos son para el proyecto Android Studio real luego de integrar los archivos.

## Diferencia con el Cotejo 30

- C30: XML `TextView` para cada registro del `RecyclerView`.
- C31: `ComposeView` aloja `LegacyInventoryComposeRow` en ese mismo `RecyclerView`; `Fragment`, FAB, NavGraph y diálogo permanecen en Views.

No es necesario incorporar Accompanist para adaptar el tema: FarmApp ya dispone de su propio `FarmAppTheme` Material 3. 
