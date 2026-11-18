
package com.example.farmapp

import android.app.Application
import android.os.Build
import android.util.Log

import com.example.farmapp.data.AppContainer
import com.example.farmapp.data.DefaultAppContainer

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage

import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize

class FarmAppApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        // 1. Inicializar Firebase antes de utilizar sus servicios.
        Firebase.initialize(context = this)

        Log.i("FirebaseConfig", "Firebase inicializado")

        // 2. Mostrar en Logcat qué proyecto Firebase está utilizando la app.
        logFirebaseConfiguration()

        // 3. Instalar App Check Debug en las compilaciones de desarrollo.
        if (BuildConfig.DEBUG) {
            Firebase.appCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )

            Log.i(
                "AppCheckTest",
                "Proveedor Debug de App Check instalado"
            )
        }

        // 4. Solo HU04 Test Debug utiliza Firebase Emulator.
        if (BuildConfig.DEBUG && BuildConfig.FLAVOR == "hu04Test") {
            configureFirebaseEmulators()
        }

        // 5. Crear el contenedor de dependencias después de configurar Firebase.
        container = DefaultAppContainer(this)

        // 6. Iniciar la sincronización del catálogo.
        (container as DefaultAppContainer)
            .catalogRealtimeSync.start()

        Log.i("FarmAppApplication", "Aplicación inicializada correctamente")
    }

    private fun logFirebaseConfiguration() {

        val options = FirebaseApp.getInstance().options

        Log.i(
            "FirebaseConfig",
            "Project ID: ${options.projectId}"
        )

        Log.i(
            "FirebaseConfig",
            "Project Number: ${options.gcmSenderId}"
        )

        Log.i(
            "FirebaseConfig",
            "Storage Bucket: ${options.storageBucket}"
        )
    }

    private fun configureFirebaseEmulators() {

        val isAndroidEmulator =
            Build.FINGERPRINT.startsWith("generic") ||
                    Build.FINGERPRINT.contains("emulator") ||
                    Build.MODEL.contains("sdk_gphone")

        val host = if (isAndroidEmulator) {
            "10.0.2.2"
        } else {
            "127.0.0.1"
        }

        // Authentication Emulator
        FirebaseAuth.getInstance()
            .useEmulator(host, 9099)

        // Firestore Emulator
        FirebaseFirestore.getInstance()
            .useEmulator(host, 8080)

        // Cloud Functions Emulator
        FirebaseFunctions.getInstance("us-central1")
            .useEmulator(host, 5001)

        // Storage Emulator: utiliza el bucket predeterminado del JSON.
        FirebaseStorage.getInstance()
            .useEmulator(host, 9199)

        Log.i(
            "FarmAppEmulator",
            "Firebase Emulator configurado en $host"
        )
    }
}
