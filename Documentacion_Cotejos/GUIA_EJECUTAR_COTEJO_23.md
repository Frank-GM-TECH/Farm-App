# GUÍA DE EJECUCIÓN — COTEJO 23

1. Descomprime el ZIP e incorpora la carpeta `app/src` en el proyecto FarmApp existente de Android Studio. Haz copia de seguridad de los archivos que ya modificaste.
2. Agrega la dependencia Coil (`io.coil-kt:coil-compose:2.7.0`) en el `app/build.gradle.kts`. El plugin de serialización y las dependencias de Retrofit, Compose, Navigation y Firebase siguen siendo necesarios.
3. Sincroniza Gradle y compila (Build > Make Project). No basta con abrir el ZIP: contiene **fuentes**, no wrapper Gradle.
4. Inicia el emulador, configura Firebase como en HU01, inicia sesión y abre Catálogo.
5. En la sección de REST, elige **Ver galería de imágenes**. La galería carga automáticamente los datos externos.
6. Comprueba `LazyVerticalGrid`: las tarjetas deben distribuirse en más columnas en tablets o pantallas anchas.
7. Usa el modo avión antes de volver a entrar en la galería; comprueba estado de error y botón **Volver a intentar** tras recuperar la conexión. Recuerda que Coil puede mostrar imágenes en caché.
8. Ejecuta las pruebas locales:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

9. Ejecuta las instrumentadas con emulador encendido:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

10. Para la exposición, abre `RemoteProductDto.kt`, `RemoteProductGalleryScreen.kt` y `FarmAppNavHost.kt`. Muestra también los recursos `ic_remote_photo_*` en Resource Manager.

**Precaución:** los productos son demostrativos y pueden cambiar en la API externa. No constituyen medicamentos, stock ni precios reales.
