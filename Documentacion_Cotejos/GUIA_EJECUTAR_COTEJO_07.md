# Ejecutar y verificar Cotejo 07 en Android Studio

1. Crear proyecto Android Studio `Empty Activity` con Kotlin + Compose y paquete `com.example.farmapp`, si todavía no se ha creado.
2. Integrar `app/src` de este ZIP sin eliminar los archivos Gradle generados por la plantilla. Seguir `README.md` para Firebase y dependencias.
3. Abrir `ui/catalog/CatalogLearningPreview.kt` y ejecutar la preview `CatalogLearningPreviewPreview()`: no requiere iniciar sesión ni Firebase.
4. Ejecutar la app autenticada: en `HomeScreen` se verá el catálogo educativo. Buscar por nombre/código y probar chips por categoría.
5. En Windows, en la terminal del proyecto con el wrapper Gradle instalado:
   - ` .\gradlew.bat :app:testDebugUnitTest ` para JUnit local.
   - iniciar emulador y ejecutar ` .\gradlew.bat :app:connectedDebugAndroidTest ` para Compose instrumentado.
6. Capturar los resultados reales: pruebas verdes, búsqueda y filtro en emulador, explicación del `CatalogAnalyzer`.

**Alcance:** productos y datos inventados para practicar Kotlin; no representa inventario real ni sistema de ventas.
