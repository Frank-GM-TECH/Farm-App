package com.example.farmapp.data.preferences

import com.example.farmapp.auth.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Falso que evita crear archivos DataStore durante las pruebas JVM de ViewModel. */
@OptIn(ExperimentalCoroutinesApi::class)
class UserPreferencesViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private class FakePreferences : UserPreferencesRepository {
        val state = MutableStateFlow(UserPreferences())
        var failWrites = false
        override val preferences = state
        override suspend fun setTheme(theme: AppThemePreference) {
            if (failWrites) throw java.io.IOException("fallo simulado")
            state.value = state.value.copy(theme = theme)
        }
        override suspend fun setGalleryLayout(layout: GalleryLayoutPreference) {
            if (failWrites) throw java.io.IOException("fallo simulado")
            state.value = state.value.copy(galleryLayout = layout)
        }
    }
    @Test fun cargaInicialDePrefs() = runTest(main.dispatcher) {
        val vm = UserPreferencesViewModel(FakePreferences())
        assertTrue(vm.uiState.value.isLoading)
        runCurrent()
        assertFalse(vm.uiState.value.isLoading)
        assertEquals(AppThemePreference.SYSTEM, vm.uiState.value.preferences.theme)
    }
    @Test fun cambioDeTemaSePublicaPorStateFlow() = runTest(main.dispatcher) {
        val fake = FakePreferences(); val vm = UserPreferencesViewModel(fake)
        runCurrent(); vm.setTheme(AppThemePreference.DARK); advanceUntilIdle()
        assertEquals(AppThemePreference.DARK, vm.uiState.value.preferences.theme)
    }
    @Test fun preferenciaListaNoSobrescribeTema() = runTest(main.dispatcher) {
        val fake = FakePreferences(); val vm = UserPreferencesViewModel(fake)
        runCurrent(); vm.setTheme(AppThemePreference.LIGHT); advanceUntilIdle()
        vm.setGalleryLayout(GalleryLayoutPreference.LIST); advanceUntilIdle()
        assertEquals(GalleryLayoutPreference.LIST, vm.uiState.value.preferences.galleryLayout)
        assertEquals(AppThemePreference.LIGHT, vm.uiState.value.preferences.theme)
    }
    @Test fun rechazaEscrituraAntesDeCarga() = runTest(main.dispatcher) {
        val fake = FakePreferences(); val vm = UserPreferencesViewModel(fake)
        vm.setTheme(AppThemePreference.DARK)
        runCurrent()
        assertEquals(AppThemePreference.SYSTEM, fake.state.value.theme)
    }
    @Test fun errorEscrituraNoDestruyeEstado() = runTest(main.dispatcher) {
        val fake = FakePreferences(); val vm = UserPreferencesViewModel(fake)
        runCurrent(); fake.failWrites = true; vm.setTheme(AppThemePreference.LIGHT); advanceUntilIdle()
        assertTrue(vm.uiState.value.hasError)
        assertEquals(AppThemePreference.SYSTEM, vm.uiState.value.preferences.theme)
    }
}
