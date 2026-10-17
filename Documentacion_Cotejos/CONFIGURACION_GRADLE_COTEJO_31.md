# Configuración requerida — Cotejo 31

No requiere nuevas bibliotecas adicionales si el Cotejo 30 y los componentes Compose previos ya están configurados correctamente.

En `app/build.gradle.kts` deben estar habilitados los dos enfoques:

```kotlin
android {
    buildFeatures {
        compose = true
        viewBinding = true
    }
}
```

Y deben mantenerse, usando versiones compatibles con el resto del proyecto:
- `androidx.compose.ui:ui`, `androidx.compose.material3:material3` y Compose BOM.
- `androidx.recyclerview:recyclerview`.
- Dependencias de Fragment, Navigation Fragment y Room de cotejos anteriores.
- `androidx.compose.ui:ui-test-junit4` para `androidTest` y `ui-test-manifest` en debug como en cotejos de UI.

**No dupliques configuraciones** ya definidas en tu proyecto; unifica las secciones existentes.
