# Gradle — Cotejo 32

Asegúrate de que tu proyecto **ya tenga los plugins y dependencias de Compose** de los cotejos anteriores y `viewBinding = true` del cotejo 30.

En `app/build.gradle.kts`:

```kotlin
android {
    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {
    // Sin versión directa si utilizas la BOM de Compose de tu proyecto:
    implementation("androidx.compose.ui:ui-viewbinding")
    // Se mantienen las demás dependencias previas (Material3, Lifecycle, Room y demás).
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

No dupliques dependencias ya existentes. Respeta la versión de Compose BOM y alinea las demás bibliotecas con tu proyecto. Las pruebas instrumentadas requieren dispositivo/emulador.
