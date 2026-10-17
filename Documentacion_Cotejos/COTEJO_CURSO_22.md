# Cotejo 22 — Cómo agregar un repositorio y DI manual

Curso: [«Cómo agregar un repositorio y una DI manual»](https://developer.android.com/codelabs/basic-android-kotlin-compose-add-repository?hl=es-419), ejemplo **Mars Photos**.

## Diagnóstico real de FarmApp versión 21

| Requisito del curso | Antes | Después |
|---|---|---|
| Capa de datos y contrato de repositorio | Interfaces `RemoteProductsRepository`, `AuthRepository` y `CatalogRepository` ya existían | Conservadas |
| `AppContainer` y `DefaultAppContainer` | No | **Añadidos** en `data/AppContainer.kt` |
| `Application` con un contenedor | No | **Añadida** `FarmAppApplication.kt` |
| Registro de Application en manifest | No | `android:name=".FarmAppApplication"` |
| Retrofit y repositorio creados en contenedor | `RemoteProductsServiceProvider` global + Factory | **Ahora en `DefaultAppContainer` con `by lazy`** |
| ViewModel recibe repositorio por constructor | Sí | Conservado; ahora lo extrae de AppContainer al crearse |
| Repositorios falsos para pruebas unitarias | Parcial | **Dos clases de test nuevas con 5 casos** |
| Tests de repositorio y ViewModel | Parciales | Se amplían sin HTTP ni Firebase |

## Patrón aplicado

```text
FarmAppApplication : Application
  └── container: AppContainer = DefaultAppContainer()
       ├── remoteProductsRepository: RemoteProductsRepository (Retrofit)
       ├── catalogRepository: CatalogRepository<DemoProduct> (local educativo)
       └── authRepository: AuthRepository (Firebase Auth)

RemoteProductsViewModel.Factory
  └── CreationExtras.farmAppContainer().remoteProductsRepository
       └── RemoteProductsViewModel(repository) → StateFlow → RemoteProductsPanel
```

El `AppContainer` solo administra dependencias, **no guarda estado de pantalla ni crea CoroutineScope global**. La capa de datos se mantiene independiente de Compose. El repositorio real de medicamentos/HU03 y roles/HU02 siguen pendientes.

## Archivos añadidos

- `app/src/main/java/com/example/farmapp/data/AppContainer.kt`
- `app/src/main/java/com/example/farmapp/data/CreationExtrasExtensions.kt`
- `app/src/main/java/com/example/farmapp/FarmAppApplication.kt`
- `app/src/test/java/com/example/farmapp/data/RemoteProductsRepositoryDiTest.kt`
- `app/src/test/java/com/example/farmapp/data/ManualDiViewModelTest.kt`

## Archivos modificados

- `AndroidManifest.xml`: registro de `FarmAppApplication`.
- `network/RemoteProductsService.kt`: se retira servicio global (lo administra AppContainer).
- `auth/AuthViewModel.kt`, `catalog/CatalogViewModel.kt`, `catalog/CatalogAvailabilityViewModel.kt` y `network/RemoteProductsViewModel.kt`: `Factory` extrae repo de `Application` sin crear la implementación concreta.

## Pruebas añadidas (pendientes de ejecutar en Gradle)

1. Repositorio transforma un DTO devuelto por servicio falso.
2. Repositorio devuelve JSON textual desde servicio falso.
3. ViewModel con contenedor falso publica SUCCESS.
4. ViewModel con contenedor falso publica ERROR.
5. Dos contenedores falsos aislados producen resultados independientes.

## Evidencia

En Android Studio muestra `AppContainer.kt`, `FarmAppApplication.kt`, `AndroidManifest.xml` y `RemoteProductsViewModel.kt`; compara sus implementaciones con los pasos del codelab. Muestra los tests falsos sin acceso a Internet.

**Pendiente:** compilar el conjunto con Gradle, ejecutar el conjunto JUnit en Android Studio, realizar la conexión HTTP de demostración y ejecutar las pruebas instrumentadas. El ZIP es fuente para integrar en una plantilla Compose, no un proyecto Gradle autónomo.
