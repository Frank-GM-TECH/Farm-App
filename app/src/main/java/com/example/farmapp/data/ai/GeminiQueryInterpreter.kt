
package com.example.farmapp.data.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.CancellationException

class GeminiQueryInterpreter {

    companion object {
        private const val TAG = "FarmAppVoiceAI"
    }

    private val model by lazy {
        Firebase.ai(
            backend = GenerativeBackend.agentPlatform()
        ).generativeModel("gemini-3.8-flash")
    }

    suspend fun interpret(spokenText: String): String {

        val originalQuery = spokenText.trim()

        if (originalQuery.isBlank()) {
            Log.w(TAG, "Consulta vacía. No se enviará solicitud.")
            return originalQuery
        }

        val prompt = """
            Eres un asistente que interpreta consultas de voz
            para buscar productos en una farmacia.

            Corrige errores fonéticos y ortográficos.
            Reconoce nombres de medicamentos y productos.
            Conserva marcas, dosis, cantidades y presentaciones.
            No inventes productos ni agregues información.
            No expliques tu respuesta.
            Devuelve únicamente el texto corregido.
            Si no puedes determinar una corrección segura,
            devuelve el texto original.

            Texto reconocido:
            "$originalQuery"
        """.trimIndent()

        return try {

            Log.d(TAG, "========================================")
            Log.d(TAG, "INICIANDO SOLICITUD A GEMINI")
            Log.d(TAG, "Texto original: $originalQuery")
            Log.d(TAG, "Modelo: gemini-3.8-flash")
            Log.d(TAG, "Backend: Agent Platform")
            Log.d(TAG, "Enviando solicitud...")

            val response = model.generateContent(prompt)

            Log.d(TAG, "RESPUESTA RECIBIDA DE GEMINI")
            Log.d(TAG, "Respuesta: ${response.text}")

            val interpretedText = response.text
                ?.trim()
                ?.trim('"', '\'', '`')
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: originalQuery

            Log.d(TAG, "Texto final: $interpretedText")
            Log.d(TAG, "SOLICITUD FINALIZADA CORRECTAMENTE")
            Log.d(TAG, "========================================")

            interpretedText

        } catch (e: CancellationException) {

            Log.w(TAG, "Solicitud a Gemini cancelada.")
            throw e

        } catch (e: Exception) {

            Log.e(
                TAG,
                "ERROR AL ENVIAR O PROCESAR LA SOLICITUD DE GEMINI",
                e
            )

            originalQuery
        }
    }
}
