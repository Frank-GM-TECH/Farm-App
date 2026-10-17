package com.example.farmapp

import android.app.Application
import com.example.farmapp.data.AppContainer
import com.example.farmapp.data.DefaultAppContainer

/** C22: el contenedor de aplicación vive tanto como el proceso Android. */
class FarmAppApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
