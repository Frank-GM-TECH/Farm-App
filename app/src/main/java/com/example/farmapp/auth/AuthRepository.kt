package com.example.farmapp.auth

/** Contrato de acceso a datos (la UI nunca depende directamente de Firebase). */
interface AuthRepository {
    fun currentUserEmail(): String?
    fun isAuthenticated(): Boolean
    suspend fun signIn(email: String, password: String)
    suspend fun register(email: String, password: String)
    suspend fun signInWithGoogleIdToken(idToken: String)
    suspend fun resetPassword(email: String)
    fun signOut()
}
