package com.example.farmapp.auth

import com.example.farmapp.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Tests locales de lógica de HU01. No conectan a Firebase. */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test fun correoInvalidoNoContactaAlRepositorio() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)
        viewModel.signIn("sin-arroba", "123456")
        advanceUntilIdle()
        assertEquals(0, repository.signInCalls)
        assertEquals(R.string.auth_error_invalid_email, viewModel.uiState.value.errorMessageRes)
    }

    @Test fun passwordCortaNoContactaAlRepositorio() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)
        viewModel.register("cliente@farmapp.pe", "12345")
        advanceUntilIdle()
        assertEquals(0, repository.registrationCalls)
        assertEquals(R.string.auth_error_password_short, viewModel.uiState.value.errorMessageRes)
    }

    @Test fun loginExitosoNormalizaCorreoYActualizaEstado() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)
        viewModel.signIn("  cliente@farmapp.pe  ", "123456")
        advanceUntilIdle()
        assertEquals(1, repository.signInCalls)
        assertEquals("cliente@farmapp.pe", repository.lastEmail)
        assertTrue(viewModel.uiState.value.isAuthenticated)
        assertEquals("cliente@farmapp.pe", viewModel.uiState.value.userEmail)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test fun fallaDeRepositorioExponeErrorSinAutenticar() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeAuthRepository().apply { shouldFail = true }
        val viewModel = AuthViewModel(repository)
        viewModel.signIn("cliente@farmapp.pe", "123456")
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isAuthenticated)
        assertEquals(R.string.auth_error_sign_in, viewModel.uiState.value.errorMessageRes)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test fun recuperarConCorreoValidoMuestraConfirmacion() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)
        viewModel.resetPassword(" cliente@farmapp.pe ")
        advanceUntilIdle()
        assertEquals(1, repository.resetCalls)
        assertEquals("cliente@farmapp.pe", repository.lastEmail)
        assertEquals(R.string.auth_info_password_reset, viewModel.uiState.value.infoMessageRes)
    }

    @Test fun cerrarSesionRestableceEstado() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeAuthRepository().apply { storedEmail = "cliente@farmapp.pe" }
        val viewModel = AuthViewModel(repository)
        assertTrue(viewModel.uiState.value.isAuthenticated)
        viewModel.signOut()
        assertEquals(1, repository.signOutCalls)
        assertFalse(viewModel.uiState.value.isAuthenticated)
        assertNull(viewModel.uiState.value.userEmail)
    }

    @Test fun limpiarMensajesDescartaError() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = AuthViewModel(FakeAuthRepository())
        viewModel.signIn("correo-no-valido", "123456")
        assertEquals(R.string.auth_error_invalid_email, viewModel.uiState.value.errorMessageRes)
        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.errorMessageRes)
    }
    @Test fun registroExitosoMarcaSesionAutenticada() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)
        viewModel.register("nuevo@farmapp.pe", "Farmacia2026")
        advanceUntilIdle()
        assertEquals(1, repository.registrationCalls)
        assertTrue(viewModel.uiState.value.isAuthenticated)
        assertEquals("nuevo@farmapp.pe", viewModel.uiState.value.userEmail)
    }

    @Test fun recuperarConCorreoInvalidoNoInvocaRepositorio() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)
        viewModel.resetPassword("correo-erroneo")
        advanceUntilIdle()
        assertEquals(0, repository.resetCalls)
        assertEquals(R.string.auth_error_reset_email, viewModel.uiState.value.errorMessageRes)
    }

}
