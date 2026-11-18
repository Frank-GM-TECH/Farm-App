
package com.example.farmapp.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

private const val GOOGLE_AUTH_TAG = "FarmAppGoogleAuth"

class MissingGoogleClientIdException : IllegalStateException(
    "Configura Firebase Google y actualiza google-services.json"
)

class GoogleCredentialSignIn(
    private val credentialManager: CredentialManager
) {

    suspend fun requestIdToken(context: Context): String {

        Log.d(GOOGLE_AUTH_TAG, "Paso 1: buscando default_web_client_id.")

        @Suppress("DiscouragedApi")
        val resourceId = context.resources.getIdentifier(
            "default_web_client_id",
            "string",
            context.packageName
        )

        if (resourceId == 0) {
            Log.e(
                GOOGLE_AUTH_TAG,
                "No existe el recurso default_web_client_id."
            )
            throw MissingGoogleClientIdException()
        }

        val clientId = context.getString(resourceId)

        if (clientId.isBlank()) {
            Log.e(GOOGLE_AUTH_TAG, "El cliente web está vacío.")
            throw MissingGoogleClientIdException()
        }

        Log.d(
            GOOGLE_AUTH_TAG,
            "Paso 2: cliente web encontrado. No se mostrará su valor."
        )

        val activity = generateSequence(context) {
            (it as? ContextWrapper)?.baseContext
        }
            .filterIsInstance<Activity>()
            .firstOrNull()
            ?: error("No hay Activity para mostrar el selector de Google")

        Log.d(
            GOOGLE_AUTH_TAG,
            "Paso 3: Activity encontrada: ${activity::class.java.simpleName}"
        )

        val googleOption = GetSignInWithGoogleOption.Builder(clientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        Log.d(
            GOOGLE_AUTH_TAG,
            "Paso 4: solicitud construida. Abriendo selector de Google..."
        )

        val result = credentialManager.getCredential(
            context = activity,
            request = request
        )

        Log.d(
            GOOGLE_AUTH_TAG,
            "Paso 5: Credential Manager devolvió una credencial."
        )

        val credential = result.credential

        Log.d(
            GOOGLE_AUTH_TAG,
            "Tipo de credencial recibida: ${credential::class.java.simpleName}"
        )

        if (
            credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            Log.e(
                GOOGLE_AUTH_TAG,
                "La credencial recibida no corresponde a un Google ID Token."
            )

            throw IllegalArgumentException(
                "Se recibió un tipo de credencial no compatible"
            )
        }

        Log.d(
            GOOGLE_AUTH_TAG,
            "Paso 6: convirtiendo la credencial en Google ID Token."
        )

        val token = GoogleIdTokenCredential
            .createFrom(credential.data)
            .idToken

        if (token.isBlank()) {
            Log.e(GOOGLE_AUTH_TAG, "Google devolvió un token vacío.")
            throw IllegalArgumentException("Token de Google vacío")
        }

        Log.d(
            GOOGLE_AUTH_TAG,
            "Paso 7: Google ID Token obtenido correctamente."
        )

        return token
    }

    suspend fun clearCredentialState() {
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
    }
}
