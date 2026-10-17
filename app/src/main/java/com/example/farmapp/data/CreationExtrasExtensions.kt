package com.example.farmapp.data

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.farmapp.FarmAppApplication

/** Acceso a las dependencias de Application desde ViewModel.Factory. */
fun CreationExtras.farmAppContainer(): AppContainer {
    val app = this[APPLICATION_KEY]
        as? FarmAppApplication
        ?: error("Configura android:name=.FarmAppApplication en AndroidManifest.xml")
    return app.container
}
