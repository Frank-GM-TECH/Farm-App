# Cotejo 15 — Pruebas de ViewModel y StateFlow

**Curso:** Android Basics with Compose, pruebas unitarias del ViewModel y estrategias de prueba, a partir del ejemplo de Unscramble.

## Diagnóstico de la versión 14

- Ya existían `AuthViewModelTest.kt` con fake de Firebase y `MainDispatcherRule.kt`.
- Ya existía `CatalogViewModelTest.kt` para el estado y filtros del catálogo ficticio.
- Las aserciones se centraban en el valor final `uiState.value`, adecuado para `StateFlow`.
- No había una **matriz explícita** de ruta de éxito / error / caso límite, ni pruebas de flujo con un colector de prueba en estos módulos.
- El paquete sigue sin Gradle autónomo, por lo que `testImplementation` y JaCoCo necesitan agregarse al proyecto creado en Android Studio.

## Cambios acumulativos

Dos clases nuevas de pruebas **JVM locales** en `app/src/test`:

- `catalog/CatalogViewModelStrategyTest.kt`: 9 casos que verifican búsqueda, estado vacío, datos vacíos, espacios, restauración errónea, eventos repetidos, independencia, colecciones `StateFlow` y estado guardado.
- `auth/AuthViewModelStrategyTest.kt`: 8 casos de contraseña de longitud límite, errores de registro/recuperación, correo vacío, solicitudes duplicadas durante `isLoading`, `StateFlow` inicial/cargando/autenticado y aislamiento.

No se cambiaron pantallas ni lógica de producción; se conservan los archivos de los 14 cotejos previos.

## Matriz de estrategia de pruebas

| Capa | Ruta éxito | Ruta error | Caso límite | Aislamiento |
|---|---|---|---|---|
| Catálogo | Buscar por SKU existente | Consulta sin coincidencia | Lista vacía, espacios, categoría restaurada inválida | ViewModels y SavedStateHandle nuevos |
| Autenticación | Inicio de sesión con contraseña de 6 caracteres | Registro o recuperación fallidos | 5 caracteres, email vacío, doble envío mientras carga | FakeAuthRepository independiente en cada prueba |
| StateFlow | Observar cambios de búsqueda y autenticación | Detectar estado error | Ignorar emisiones intermedias confluentes no garantizadas | `backgroundScope` limpia el colector al finalizar |

## Características de buenas pruebas

- **Enfocadas:** cada método examina un comportamiento independiente.
- **Comprensibles:** se llaman `exito_...`, `error_...` o `limite_...` y utilizan aserciones concretas.
- **Deterministas:** no hay `Thread.sleep`, Firebase, red, reloj externo ni datos de producción.
- **Independientes:** se crea un nuevo repositorio fake y un nuevo ViewModel en cada método.
- **StateFlow:** uso de `backgroundScope`, `UnconfinedTestDispatcher` y `toList` para observar cambios sin bloqueos. Las aserciones finales verifican también `uiState.value` en los tests existentes.

## Dependencias de Gradle y cobertura

Consulta `CONFIGURACION_GRADLE_PRUEBAS_15.md`:
- `testImplementation` para JUnit y `kotlinx-coroutines-test`;
- `androidTestImplementation` para JUnit Android y Compose;
- `enableUnitTestCoverage` y `enableAndroidTestCoverage`;
- comandos Gradle de ejecución y de reportes de cobertura.

**No se establece un porcentaje de cobertura no medido.** Cuando ejecutes Android Studio, comprueba porcentaje de líneas y ramas cubiertas y añade pruebas para ramas rojas/amarillas.

## Demostración a la profesora

1. Abre `AuthViewModelStrategyTest.kt`: explica `MainDispatcherRule`, el repositorio falso y el caso de doble envío suspendido con `CompletableDeferred`.
2. Abre `CatalogViewModelStrategyTest.kt`: muestra el caso `stateFlow_publicaEstadoInicialBusquedaYCategoria()` y la diferencia entre estado inicial y final.
3. Abre `CONFIGURACION_GRADLE_PRUEBAS_15.md` y enseña `testImplementation`.
4. Ejecuta `:app:testDebugUnitTest` en Android Studio y captura el resultado.
5. Ejecuta `:app:createDebugUnitTestCoverageReport` y captura el porcentaje real obtenido; revisa líneas sin cubrir.

## Limitación de la entrega

Este cotejo contiene nuevas pruebas JUnit que deberán compilarse y ejecutarse en un proyecto Android Studio con Gradle y dependencias reales. La ejecución local de validaciones Kotlin puras no sustituye la suite JUnit del IDE ni demuestra una cobertura porcentual.
