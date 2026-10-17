package com.example.farmapp

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass

/**
 * Cotejo 13: callbacks reales de Activity; inspeccionables en Logcat.
 * Mantener la instrumentación ligera: no iniciar consultas o tareas duplicadas
 * en onResume()/onStart(); ViewModel y Compose se encargan de sus propios estados.
 */
class MainActivity : ComponentActivity() {
    private companion object {
        const val TAG = "FarmAppLifecycle"
    }

    /** Solo informamos del nombre del evento: nunca datos de usuario ni credenciales. */
    private fun registrar(evento: String) {
        if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            Log.d(TAG, evento)
        }
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registrar(
            if (savedInstanceState == null) "onCreate: inicio de Activity"
            else "onCreate: Activity recreada con estado guardado"
        )
        enableEdgeToEdge()
        // El tamaño se vuelve a leer al recomponer/redimensionar la ventana.
        setContent {
            val windowSize = calculateWindowSizeClass(this@MainActivity)
            FarmAppRoot(windowWidthSizeClass = windowSize.widthSizeClass)
        }
    }

    override fun onStart() {
        super.onStart()
        registrar("onStart: Activity visible")
    }

    override fun onResume() {
        super.onResume()
        registrar("onResume: Activity interactiva")
    }

    override fun onPause() {
        registrar("onPause: Activity pierde el foco")
        super.onPause()
    }

    override fun onStop() {
        registrar("onStop: Activity deja de ser visible")
        super.onStop()
    }

    override fun onRestart() {
        super.onRestart()
        registrar("onRestart: Activity regresa luego de onStop")
    }

    override fun onDestroy() {
        registrar(
            if (isChangingConfigurations) "onDestroy: cambio de configuración"
            else "onDestroy: destrucción solicitada por Android"
        )
        super.onDestroy()
    }
}
