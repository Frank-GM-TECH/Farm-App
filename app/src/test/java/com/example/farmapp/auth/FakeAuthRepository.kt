package com.example.farmapp.auth

/** Doble de pruebas: no realiza llamadas a Firebase ni a la red. */
internal class FakeAuthRepository : AuthRepository {
    var storedEmail: String? = null
    var shouldFail: Boolean = false
    var signInCalls: Int = 0
    var registrationCalls: Int = 0
    var resetCalls: Int = 0
    var signOutCalls: Int = 0
    var lastEmail: String? = null
    var googleSignInCalls: Int = 0
    var receivedGoogleToken: String? = null

    override fun currentUserEmail(): String? = storedEmail
    override fun isAuthenticated(): Boolean = storedEmail != null

    override suspend fun signIn(email: String, password: String) {
        signInCalls++
        lastEmail = email
        if (shouldFail) error("Fallo de red simulado")
        storedEmail = email
    }

    override suspend fun register(email: String, password: String) {
        registrationCalls++
        lastEmail = email
        if (shouldFail) error("Fallo de registro simulado")
        storedEmail = email
    }

    override suspend fun signInWithGoogleIdToken(idToken: String) {
        googleSignInCalls++
        receivedGoogleToken = idToken
        if (shouldFail) error("Firebase rechazó la credencial simulada")
        storedEmail = "usuario.google@ejemplo.com"
    }
    override suspend fun resetPassword(email: String) {
        resetCalls++
        lastEmail = email
        if (shouldFail) error("Fallo de recuperación simulado")
    }

    override fun signOut() {
        signOutCalls++
        storedEmail = null
    }
}
