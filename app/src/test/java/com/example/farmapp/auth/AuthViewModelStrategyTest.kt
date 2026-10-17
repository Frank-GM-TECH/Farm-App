package com.example.farmapp.auth

import com.example.farmapp.R
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Cotejo 15: rutas de éxito, error, límite y emisión observable de StateFlow.
 * Repositorios fake nuevos por prueba; cero tráfico a Firebase.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelStrategyTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test fun exito_contrasenaDeSeisCaracteresEsAceptada() = runTest(mainDispatcherRule.dispatcher) {
        val fake = FakeAuthRepository()
        val vm = AuthViewModel(fake)
        vm.signIn("cliente@farmapp.pe", "123456")
        advanceUntilIdle()
        assertEquals(1, fake.signInCalls)
        assertTrue(vm.uiState.value.isAuthenticated)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test fun error_registroFallidoExponeMensajeSinAutenticar() = runTest(mainDispatcherRule.dispatcher) {
        val fake = FakeAuthRepository().apply { shouldFail = true }
        val vm = AuthViewModel(fake)
        vm.register("nuevo@farmapp.pe", "123456")
        advanceUntilIdle()
        assertEquals(1, fake.registrationCalls)
        assertFalse(vm.uiState.value.isAuthenticated)
        assertFalse(vm.uiState.value.isLoading)
        assertEquals(R.string.auth_error_sign_in, vm.uiState.value.errorMessageRes)
    }

    @Test fun error_recuperacionFallidaMuestraErrorYApagaCarga() = runTest(mainDispatcherRule.dispatcher) {
        val fake = FakeAuthRepository().apply { shouldFail = true }
        val vm = AuthViewModel(fake)
        vm.resetPassword("cliente@farmapp.pe")
        advanceUntilIdle()
        assertEquals(1, fake.resetCalls)
        assertEquals(R.string.auth_error_reset_request, vm.uiState.value.errorMessageRes)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test fun limite_contrasenaDeCincoCaracteresNoLlamaAlRepositorio() = runTest(mainDispatcherRule.dispatcher) {
        val fake = FakeAuthRepository()
        val vm = AuthViewModel(fake)
        vm.signIn("cliente@farmapp.pe", "12345")
        advanceUntilIdle()
        assertEquals(0, fake.signInCalls)
        assertEquals(R.string.auth_error_password_short, vm.uiState.value.errorMessageRes)
    }

    @Test fun limite_correoEnBlancoNoLlamaAlRepositorio() = runTest(mainDispatcherRule.dispatcher) {
        val fake = FakeAuthRepository()
        val vm = AuthViewModel(fake)
        vm.signIn("   ", "123456")
        advanceUntilIdle()
        assertEquals(0, fake.signInCalls)
        assertEquals(R.string.auth_error_invalid_email, vm.uiState.value.errorMessageRes)
    }

    @Test fun limite_solicitudDuplicadaDuranteCargaSeIgnora() = runTest(mainDispatcherRule.dispatcher) {
        val fake = DelayedSignInRepository()
        val vm = AuthViewModel(fake)
        vm.signIn("primero@farmapp.pe", "123456")
        runCurrent() // Ejecuta el inicio de la corrutina, que queda suspendida en el fake.
        assertTrue(vm.uiState.value.isLoading)
        assertEquals(1, fake.signInCalls)
        vm.signIn("segundo@farmapp.pe", "123456")
        assertEquals(1, fake.signInCalls)
        fake.release()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isAuthenticated)
        assertEquals("primero@farmapp.pe", vm.uiState.value.userEmail)
        assertEquals(1, fake.signInCalls)
    }

    @Test fun stateFlow_notificaEstadoInicialCargaYResultado() = runTest(mainDispatcherRule.dispatcher) {
        val fake = DelayedSignInRepository()
        val vm = AuthViewModel(fake)
        val observed = mutableListOf<AuthUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.toList(observed)
        }
        assertFalse(observed.last().isAuthenticated)
        vm.signIn("cliente@farmapp.pe", "123456")
        runCurrent()
        assertTrue(observed.last().isLoading)
        fake.release()
        advanceUntilIdle()
        assertTrue(observed.last().isAuthenticated)
        assertFalse(observed.last().isLoading)
    }

    @Test fun aislamiento_credencialesDeUnaPruebaNoFiltranAOtroViewModel() = runTest(mainDispatcherRule.dispatcher) {
        val first = AuthViewModel(FakeAuthRepository())
        val second = AuthViewModel(FakeAuthRepository())
        first.signIn("cliente@farmapp.pe", "123456")
        advanceUntilIdle()
        assertTrue(first.uiState.value.isAuthenticated)
        assertFalse(second.uiState.value.isAuthenticated)
        assertNull(second.uiState.value.userEmail)
    }

    private class DelayedSignInRepository : AuthRepository {
        private val gate = CompletableDeferred<Unit>()
        private var email: String? = null
        var signInCalls = 0
            private set

        override fun currentUserEmail(): String? = email
        override fun isAuthenticated() = email != null
        override suspend fun signIn(email: String, password: String) {
            signInCalls++
            gate.await() // Suspensión controlada, NO Thread.sleep().
            this.email = email
        }
        override suspend fun register(email: String, password: String) = signIn(email, password)
        override suspend fun signInWithGoogleIdToken(idToken: String) { error("Google no simulado") }
        override suspend fun resetPassword(email: String) = Unit
        override fun signOut() { email = null }
        fun release() { gate.complete(Unit) }
    }
}
