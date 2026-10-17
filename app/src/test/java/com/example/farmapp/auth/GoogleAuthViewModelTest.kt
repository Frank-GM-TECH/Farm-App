package com.example.farmapp.auth

import com.example.farmapp.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Pruebas sin tokens reales, red ni credenciales de Google. */
@OptIn(ExperimentalCoroutinesApi::class)
class GoogleAuthViewModelTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    @Test fun tokenValidoAutenticaConElRepositorio() = runTest(dispatcherRule.dispatcher) {
        val fake = FakeAuthRepository()
        val vm = AuthViewModel(fake)
        vm.signInWithGoogleIdToken("id-token-de-prueba")
        advanceUntilIdle()
        assertEquals(1, fake.googleSignInCalls)
        assertTrue(vm.uiState.value.isAuthenticated)
        assertEquals("usuario.google@ejemplo.com", vm.uiState.value.userEmail)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test fun tokenVacioNoContactaFirebase() = runTest(dispatcherRule.dispatcher) {
        val fake = FakeAuthRepository()
        val vm = AuthViewModel(fake)
        vm.signInWithGoogleIdToken("  ")
        advanceUntilIdle()
        assertEquals(0, fake.googleSignInCalls)
        assertEquals(R.string.auth_error_google, vm.uiState.value.errorMessageRes)
    }

    @Test fun falloFirebaseMuestraErrorYNoAutentica() = runTest(dispatcherRule.dispatcher) {
        val fake = FakeAuthRepository().apply { shouldFail = true }
        val vm = AuthViewModel(fake)
        vm.signInWithGoogleIdToken("id-token-de-prueba")
        advanceUntilIdle()
        assertEquals(1, fake.googleSignInCalls)
        assertFalse(vm.uiState.value.isAuthenticated)
        assertFalse(vm.uiState.value.isLoading)
        assertEquals(R.string.auth_error_google, vm.uiState.value.errorMessageRes)
    }

    @Test fun muestraErrorConfiguracionSinIntentarAcceso() = runTest(dispatcherRule.dispatcher) {
        val fake = FakeAuthRepository()
        val vm = AuthViewModel(fake)
        vm.reportGoogleCredentialError(configurationMissing = true)
        assertEquals(R.string.auth_error_google_setup, vm.uiState.value.errorMessageRes)
        assertEquals(0, fake.googleSignInCalls)
    }

    @Test fun cerrarSesionGoogleLimpiaEstadoDeSesionFirebase() = runTest(dispatcherRule.dispatcher) {
        val fake = FakeAuthRepository()
        val vm = AuthViewModel(fake)
        vm.signInWithGoogleIdToken("id-token-de-prueba")
        advanceUntilIdle()
        vm.signOut()
        assertFalse(vm.uiState.value.isAuthenticated)
        assertEquals(1, fake.signOutCalls)
    }
}
