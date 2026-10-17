# FARM APP — COTEJO N.º 06: Pruebas automatizadas (Android Basics with Compose)

## Objetivo de la lección
Aprender qué son los tests automatizados, diferencias entre JVM (`test`) e instrumentación Android (`androidTest`), métodos `@Test`, aserciones, reglas `@Rule`, `createComposeRule`, interacciones y ejecución de pruebas.

## Comparación antes y después
| Requisito | Estado en Cotejo 05 | Cotejo 06 (actual) | Evidencia |
|---|---|---|---|
| Tests locales | Sí, 3 clases | Ampliados | `app/src/test/.../AuthValidatorTest.kt`, `PasswordAdvisorTest.kt` |
| Tests del ViewModel | No | Añadidos con FakeAuthRepository | `app/src/test/.../AuthViewModelTest.kt` |
| Regla de pruebas con dispatcher | No | Añadida | `app/src/test/.../MainDispatcherRule.kt` |
| Tests instrumentados | Sí, 2 clases | 3 clases | `app/src/androidTest/.../AuthScreenInteractionTest.kt` |
| Métodos `@Test` | Sí | Añadidos casos de límites, errores, flujo correcto | Buscar `@Test` |
| Aserciones | Sí | assertEquals/assertTrue/assertFalse/assertNull/assertExists/assertIsNotEnabled | clases de prueba |
| Regla ComposeTestRule | Sí | Preservada con `createComposeRule()` | clases androidTest |
| Acciones de UI | Sí | ingresar texto, pulsar, alternar registro, deshabilitar en carga | `AuthScreenInteractionTest.kt` |
| Identificadores estables para UI | Parcial | Añadidos `testTag` para correo, envío, cambio de modo, mensaje y recuperación | `AuthScreen.kt` |
| Ejecución reproducible | Insuficiente | Añadida guía detallada y comandos | `GUIA_EJECUTAR_PRUEBAS_06.md` |

## Ejemplos concretos para exponer

**Ejemplo local:** `AuthValidatorTest.correoSinDominioEsRechazado()` verifica una regla Kotlin con `assertFalse`, sin pantalla Android.

**Ejemplo ViewModel:** `AuthViewModelTest.loginExitosoNormalizaCorreoYActualizaEstado()` sustituye Firebase por `FakeAuthRepository`, usa `MainDispatcherRule`, comprueba que el correo se normalizó y que `uiState` se actualizó.

**Ejemplo instrumentado:** `AuthScreenInteractionTest.completarFormularioYEnviarLoginInvocaCallbackConValoresCorrectos()` inicia un árbol Compose con `setContent`, introduce correo y contraseña con `performTextInput`, pulsa el botón con `performClick` y verifica los datos enviados al callback con `assertEquals`. No inicia sesión ni conecta a Firebase.

## Buenas prácticas demostradas
- Nombre de prueba que explica el comportamiento esperado; cada prueba verifica un caso claro.
- Aserciones explícitas (no basta con que la prueba no crashee).
- Independencia entre tests: cada uno crea sus propias instancias y datos.
- Uso de `FakeAuthRepository` en vez de red y credenciales reales.
- Casos positivos, negativos, límites y estados de carga/error.
- IDs de recursos de texto y etiquetas `testTag` estables para interactuar con la UI.
- División de responsabilidad: pruebas JVM para negocio, instrumentación para Compose.

## Límites de verificación
- Los nuevos tests Android y ViewModel no se han ejecutado con Gradle aquí: falta Android SDK y Gradle en el entorno.
- Las pruebas locales puras Kotlin pueden compilarse y ejecutarse por separado con `kotlinc`; consulta `VALIDACION_06.txt`.
- No hay cobertura real del backend Firebase ni integración de Google Services todavía. Requiere tu configuración y emulador.
- Este ZIP sigue siendo un paquete de fuentes `app/src` para pegar en `Empty Activity`, no un proyecto Android Studio/Gradle completo.

## Próximos sprints
- HU02: tests de roles, permisos y perfil (Fake repository + estado de ViewModel).
- HU03: pruebas del filtro/catálogo y escaneo de códigos.
- HU04: pruebas de totales de carrito, cantidades, persistencia y ausencia de stock.
- Sprint 2 y 3: aislar reglas de inventario, entrega, OCR y chatbot; crear tests antes de cada nueva funcionalidad.
