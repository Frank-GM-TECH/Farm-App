# Dependencias adicionales para el Cotejo 30 — Views

Agrega las líneas siguientes en el proyecto Android Studio que ya incluya todas las dependencias anteriores.

```kotlin
android {
    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {
    // Navigation de Fragments; debe compartir versión con Navigation Compose.
    implementation("androidx.navigation:navigation-fragment-ktx:2.10.2")

    // RecyclerView y FAB de Material Components basado en Views.
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("com.google.android.material:material:1.12.0")

    // Fragment con ViewBinding y coroutines lifecycle (pueden existir ya transitivamente).
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")

    // Pruebas instrumentadas de Views.
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:core-ktx:1.7.0")
}
```

Usa las versiones del catálogo del proyecto si ya las gestionas con BOM o Version Catalog; no dupliques una dependencia existente. Es importante que la versión de `navigation-fragment-ktx` coincida con tu `navigation-compose` actual. Mantén KSP, Room, Kotlin, Compose y Firebase de los cotejos anteriores.

No se incorpora Hilt: el acceso a Room sigue viniendo del contenedor manual `FarmAppApplication.container` (cotejo 22).
