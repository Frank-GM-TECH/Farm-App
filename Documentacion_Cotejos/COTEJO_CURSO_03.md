# Cotejo 03 — Kotlin: condicionales, null-safety, POO y funciones de orden superior

**Contexto:** Kotlin para Android, cuatro capturas de resumen del curso *Aspectos básicos de Android con Compose* (ruta de fundamentos de Kotlin). Se parte del código del Cotejo 02, sin eliminar el login/registro de Firebase, el logo ni los recursos de texto.

**Funcionalidad real añadida:** en **Crear cuenta** aparece una orientación de contraseña con tres segmentos y tres sugerencias. La longitud mínima de HU01 sigue siendo **seis caracteres**; letras y números son recomendaciones (no nuevas condiciones para registrarse). Se incluye una clase para normalizar correos al autenticar y recuperar contraseñas. No se ingresan contraseñas a registros de depuración ni se almacenan localmente.

## 1. `if`, `else if`, `else`, `when` y rangos

| Requisito de las imágenes | Antes | Ahora y ubicación |
|---|---|---|
| `if` / `else` como sentencia o expresión | Ya existía para alternar registro/login | `PasswordAdvisor.evaluate()` usa `if / else if / else` como **expresión**, devolviendo `PasswordStrength` |
| `else if` | No estaba explicitado | `auth/password/PasswordAdvisor.kt` clasifica contraseña vacía, corta, básica o mejorada |
| `when` con varias ramas | No había ejemplo claro | `ui/components/PasswordGuidance.kt` elige textos de fortaleza y de recomendaciones |
| `when` con coma | Faltaba | `PasswordStrength.EMPTY, PasswordStrength.SHORT -> ...` |
| Rango `in` | No había ejemplo explícito | `password.length in 1 until minimumLength` en `PasswordAdvisor.kt` |
| `when` con `is` | Faltaba | `when (check)` distingue `is RequirementCheck.Met` y `is RequirementCheck.Missing` |

`when` y `if/else` permiten elegir qué texto y estado mostrar sin repetir lógica. La clasificación «mejorada» es orientativa, no una garantía de seguridad.

## 2. Nulos (`null`, `?.`, `?:`)

| Concepto | Evidencia |
|---|---|
| `String?`, `Int?` | `auth/AuthUiState.kt`: `userEmail`, `errorMessageRes`, `infoMessageRes` |
| `?.` llamada segura | `FarmAppRoot.kt`: `uiState.userEmail?.takeIf { ... }` |
| `?:` Elvis | `FarmAppRoot.kt`: valor por defecto `R.string.home_email_unknown` cuando no hay correo |
| `?.let` sobre anulables | `ui/auth/AuthScreen.kt`: mensajes de error/información opcionales |
| Comprobación de nulos | `auth/FirebaseAuthRepository.kt`: consulta `auth.currentUser?.email` y `auth.currentUser != null` |
| Operador `!!` | **No se incorpora deliberadamente**, porque fuerza un valor no nulo y puede provocar un fallo en ejecución; en su lugar se usan `?.`, `?:` y condiciones |

Kotlin impide asignar `null` a `String` sin `?` mediante el compilador. `String?` se usa para información que legítimamente puede estar ausente.

## 3. POO: clases, encapsulamiento, herencia, abstracción, polimorfismo, composición y propiedades

| Concepto | Evidencia en el proyecto |
|---|---|
| `class` y constructor primario | `EmailDraft(initialValue: String)` y `PasswordAdvisor(minimumLength: Int = ...)` |
| `data class` | `AuthUiState`, `PasswordAssessment`, `RequirementCheck.Met/Missing` |
| Encapsulamiento | `private val rules` de `PasswordAdvisor` y `private val auth` de `FirebaseAuthRepository` |
| Abstracción | `abstract class PasswordRule` y `interface AuthRepository` |
| Herencia **IS-A** | `LengthRule`, `LetterRule`, `NumberRule` heredan de `PasswordRule`; `AuthViewModel` hereda de `ViewModel` |
| Polimorfismo | Colección `List<PasswordRule>` llama a `matches()` de cada subclase; `FirebaseAuthRepository` implementa `AuthRepository` |
| Composición **HAS-A** | `PasswordAdvisor` contiene `rules`; `AuthViewModel` mantiene una referencia a `AuthRepository` |
| `public`, `private`, `protected`, `internal` | `PasswordAdvisor` público (visibilidad por defecto); `rules` privado; `hasContent()` protegido; `PasswordRule` y subclases internas al módulo |
| Getter personalizado | `PasswordAssessment.completedChecks` calcula el resultado al leerlo |
| Setter personalizado | `EmailDraft.value` elimina espacios externos; se usa en `AuthViewModel` para acceso y restablecimiento |
| Delegación de propiedades | `private val rules ... by lazy { ... }` en `PasswordAdvisor`; `by rememberSaveable` ya existía en `AuthScreen` |

El diseño separa la validación de contraseñas de la UI. En un futuro el mismo patrón permitirá sustituir repositorios de pruebas por reales y reutilizar reglas sin copiar código.

## 4. Lambdas y funciones de orden superior

| Punto del curso | Ejemplo que puedes enseñar |
|---|---|
| Funciones como valores | `val checker: (String) -> Boolean = AuthValidator.minimumLengthChecker(8)` en `PasswordAdvisorTest.kt` |
| Función que devuelve función | `AuthValidator.minimumLengthChecker(minimum)` devuelve `(String) -> Boolean` y se utiliza realmente en `AuthValidator.isValidPassword()` |
| Lambdas con `it` | `password.any { it.isDigit() }`, `checks.count { it is RequirementCheck.Met }` |
| Lambdas con parámetros explícitos | `rules.map { rule -> ... }` |
| Lambdas finales | `Button(onClick = { ... }) { ... }` y `repeat(3) { segment -> ... }` |
| Funciones de orden superior | `map`, `count`, `forEach`, `filter` (si corresponde), `repeat` en el indicador |
| Funciones Compose pasadas como parámetros | `FarmAppTheme(content: @Composable () -> Unit)` y acciones `(String, String) -> Unit` de `AuthScreen` |

**Sobre `repeat`:** se usa para dibujar **tres** barras en `PasswordGuidance.kt`; su lambda se ejecuta una vez por barra. El color depende de los segmentos indicados por el evaluador.

## 5. Dónde lo ve la profesora

1. Abre la app, selecciona **Crear cuenta**. Debajo de Contraseña debe aparecer la orientación con barras y requisitos.
2. Escribe una contraseña corta, otra con seis caracteres y otra como `Farmacia2026` para comparar los estados. No son contraseñas reales y no se guardan.
3. Abre `ui/components/PasswordGuidance.kt`: `when`, `is`, `repeat`, `forEach`, lambdas y `stringResource`.
4. Abre `auth/password/PasswordAdvisor.kt`: `if/else if`, `in`, clases abstractas, herencia, listas de reglas y `by lazy`.
5. Abre `auth/EmailDraft.kt`: constructor, `get()` personalizado y `set(...)` personalizado; abre `AuthViewModel.kt` para demostrar que se utiliza en el flujo real.
6. Abre `FarmAppRoot.kt` y `AuthUiState.kt` para explicar `String?`, `?.`, `?:` y ausencia de `!!`.
7. Abre `AuthValidator.kt`: función que devuelve otra función. Ejecuta las pruebas en Android Studio una vez integrado el paquete.
8. Abre `PasswordGuidancePreview()` desde **Split / Design** para enseñar el ejemplo UI sin iniciar sesión en Firebase.

## 6. Pruebas y límites

- **Verificado en esta entrega:** compilación JVM de las tres clases Kotlin puras (`AuthValidator.kt`, `EmailDraft.kt`, `PasswordAdvisor.kt`), ejecución de 15 comprobaciones funcionales, parseo de `strings.xml` y del drawable, y verificación de referencias a recursos.
- **Pendiente:** compilación de todo el módulo Android, sincronización de Gradle, pruebas JUnit desde Android Studio, vistas Compose en emulador, Firebase y comprobación visual de la pantalla.
- **Formato:** fuentes para copiar a un nuevo proyecto `Empty Activity` con Compose (`com.example.farmapp`), **no** un proyecto Gradle independiente. Sigue el `README.md`.

## 7. Aplicaciones en próximas historias de usuario

- **HU02 perfiles y roles:** `when` para representaciones de rol, `data class` para perfil, `String?` para datos opcionales, `AuthRepository` para obtener identidad. La autorización de roles elevados siempre deberá verificarse en servidor, no en un `when` de la UI.
- **HU03 catálogo e inventario:** clases de medicamento, herencia solo cuando exista una relación IS-A real, listas con `filter`, `map` y callbacks de Compose.
- **HU04 carrito:** funciones de orden superior para calcular subtotales y `?:` para datos opcionales.
- **HU05–HU07 delivery/inventario/recetas:** `when` para estados, repositorios abstractos, datos anulables y validaciones de dominio.
- **HU08–HU12 OCR/chatbot/voz/dashboard:** resultados sellados con `when (x) { is ... }`, `?.` para respuestas ausentes y `repeat()` cuando exista una cantidad real de elementos que renderizar.

La correspondencia se actualizará cuando cada historia se implemente. Estos son **usos previstos**, no funcionalidades que ya estén completadas.
