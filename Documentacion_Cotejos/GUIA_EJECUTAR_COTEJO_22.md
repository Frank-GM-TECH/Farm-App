# Guía Cotejo 22 — DI manual con repositorios

1. Crea o abre el proyecto de Android Studio `FarmApp` (`com.example.farmapp`) con Compose y API 26+.
2. Copia o fusiona todo `app/src` del paquete C22. **Conserva** la carpeta Gradle del proyecto real y las dependencias necesarias de los cotejos previos.
3. Comprueba `app/src/main/AndroidManifest.xml`: `<application android:name=".FarmAppApplication" ... />`.
4. Abre `data/AppContainer.kt`. Explica la interfaz `AppContainer`, `DefaultAppContainer` y la creación diferida `by lazy`.
5. Abre `FarmAppApplication.kt`. Observa el contenedor creado una sola vez en `onCreate()`.
6. Abre `network/RemoteProductsViewModel.kt`. Su Factory usa `this.farmAppContainer().remoteProductsRepository`; ya no construye Retrofit.
7. Repite con `auth/AuthViewModel.kt` y `catalog/CatalogViewModel.kt`.
8. En `app/src/test`, ejecuta las clases `RemoteProductsRepositoryDiTest` y `ManualDiViewModelTest`. Comprueba que funcionan sin Internet ni Firebase en los tests.
9. Para las pruebas locales, usa desde la raíz del proyecto `./gradlew :app:testDebugUnitTest` (Windows ` .\gradlew.bat :app:testDebugUnitTest`).
10. Ejecuta FarmApp (requiere **tu** archivo `google-services.json` y Firebase correctamente configurado). En Catálogo, presiona «Consultar Internet»; debe seguir funcionando como en Cotejo 21.

### Errores comunes

- Si aparece `Configura android:name=.FarmAppApplication`, debes fusionar correctamente el manifest (no mantener otro `<application>`).
- Si Firebase indica configuración ausente, verifica `google-services.json` y el plugin Gradle de Google Services.
- Si las pruebas intentan conectarse a Internet, revisa que hayan recibido `FakeService` o `FakeAppContainer` y no `DefaultAppContainer`.
- Las vistas previas usan datos de muestra; no deben llamar a `RemoteProductsViewModel.Factory` sin un ViewModelStoreOwner adecuado.

### Qué presentar ante la profesora

La arquitectura separada `UI → ViewModel → Repository → DataSource` y `Application → AppContainer`; los tests falsos muestran por qué DI manual mejora la facilidad de pruebas y la escalabilidad.
