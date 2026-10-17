package com.example.farmapp.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/** Indica que falta Google habilitado/registrado; no intenta autenticación inválida. */
class MissingGoogleClientIdException : IllegalStateException(
    "Configura Firebase Google y actualiza google-services.json"
)

/**
 * Abre el selector oficial de Google, iniciado por el botón de HU01.
 * No persiste tokens; la autenticación real la realiza FirebaseAuthRepository.
 */
class GoogleCredentialSignIn(private val credentialManager: CredentialManager) {
    suspend fun requestIdToken(context: Context): String {
        // google-services genera default_web_client_id a partir del JSON de Firebase.
        // Se busca dinámicamente para que las vistas Preview no requieran configuración Firebase.
        @Suppress("DiscouragedApi")
        val resourceId = context.resources.getIdentifier(
            "default_web_client_id", "string", context.packageName
        )
        if (resourceId == 0) throw MissingGoogleClientIdException()
        val clientId = context.getString(resourceId)
        if (clientId.isBlank()) throw MissingGoogleClientIdException()

        // Credential Manager debe utilizar el contexto de una Activity para mostrar el selector.
        val activity = generateSequence(context) { (it as? ContextWrapper)?.baseContext }
            .filterIsInstance<Activity>()
            .firstOrNull() ?: error("No hay Activity para mostrar el selector de Google")
        val googleOption = GetSignInWithGoogleOption.Builder(clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()
        val credential = credentialManager.getCredential(context = activity, request = request).credential
        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            throw IllegalArgumentException("Se recibió un tipo de credencial no compatible")
        }
        val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
        if (token.isBlank()) throw IllegalArgumentException("Token de Google vacío")
        return token
    }

    suspend fun clearCredentialState() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}
