# Dependencias Cotejo 29

No se requieren bibliotecas nuevas con respecto al Cotejo 28. Se mantiene:

```kotlin
val workVersion = "2.11.2"
implementation("androidx.work:work-runtime-ktx:$workVersion")
androidTestImplementation("androidx.work:work-testing:$workVersion")
```

Usa las versiones efectivamente configuradas en tu proyecto y sincroniza Gradle. `TestListenableWorkerBuilder`, `WorkManagerTestInitHelper` y `TestDriver` vienen del artefacto de pruebas. No dupliques la inicialización de WorkManager en producción.
