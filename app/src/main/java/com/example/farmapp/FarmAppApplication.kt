
package com.example.farmapp

import android.app.Application
import android.os.Build
import android.util.Log
import com.example.farmapp.data.AppContainer
import com.example.farmapp.data.DefaultAppContainer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.example.farmapp.BuildConfig
import com.google.firebase.storage.FirebaseStorage
class FarmAppApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        // Solo HU04 Test Debug utiliza Firebase Emulator.
        if (BuildConfig.DEBUG && BuildConfig.FLAVOR == "hu04Test") {
            configureFirebaseEmulators()
        }

        container = DefaultAppContainer(this)

        (container as DefaultAppContainer)
            .catalogRealtimeSync.start()
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


        FirebaseAuth.getInstance()
            .useEmulator(host, 9099)

        FirebaseFirestore.getInstance()
            .useEmulator(host, 8080)

        FirebaseFunctions.getInstance("us-central1")
            .useEmulator(host, 5001)

// HU02: Fotografías de perfil en Storage Emulator
        FirebaseStorage.getInstance(
            "gs://farm-app-c5291.firebasestorage.app"
        ).useEmulator(host, 9199)


        Log.i(
            "FarmAppEmulator",
            "Firebase Emulator configurado en $host"
        )
    }
}
