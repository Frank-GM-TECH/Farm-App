package com.example.farmapp.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/** Implementación real con Firebase Authentication. */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth
) : AuthRepository {
    override fun currentUserEmail(): String? = auth.currentUser?.email

    override fun isAuthenticated(): Boolean = auth.currentUser != null

    override suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    override suspend fun signInWithGoogleIdToken(idToken: String) {
        // Firebase verifica la credencial de Google antes de conceder una sesión.
        require(idToken.isNotBlank()) { "Se requiere token de Google" }
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
    }

    override suspend fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    override fun signOut() = auth.signOut()
}
