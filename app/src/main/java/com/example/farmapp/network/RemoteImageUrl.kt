package com.example.farmapp.network

/** Las URLs son datos externos, no fuentes de verdad de FarmApp.
 * No solicitamos imágenes mediante HTTP sin cifrar. Coil manejará los errores.
 */
fun String?.asSecureImageUrl(): String? = this
    ?.trim()
    ?.takeIf { it.startsWith("https://", ignoreCase = true) && it.length > 8 }
