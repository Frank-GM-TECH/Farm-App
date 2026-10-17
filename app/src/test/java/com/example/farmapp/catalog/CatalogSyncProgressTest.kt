package com.example.farmapp.catalog

import com.example.farmapp.auth.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** C20: pruebas con reloj virtual, como Race Tracker del curso. */
@OptIn(ExperimentalCoroutinesApi::class)
class CatalogSyncProgressTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    @Test fun inicio_actualizaAmbasBarrasEnParalelo() = runTest(dispatcherRule.dispatcher) {
        val vm = CatalogSyncViewModel()
        vm.startOrResume()
        assertEquals(SyncPhase.RUNNING, vm.uiState.value.phase)
        runCurrent()
        advanceTimeBy(200)
        runCurrent()
        assertEquals(10, vm.uiState.value.catalogProgress)
        assertEquals(20, vm.uiState.value.stockProgress)
        vm.reset() // Evita dejar trabajos activos después de la prueba.
        advanceUntilIdle()
    }

    @Test fun final_lasDosLleganACienSinSumarRetardos() = runTest(dispatcherRule.dispatcher) {
        val vm = CatalogSyncViewModel()
        vm.startOrResume()
        advanceUntilIdle()
        assertEquals(100, vm.uiState.value.catalogProgress)
        assertEquals(100, vm.uiState.value.stockProgress)
        assertEquals(SyncPhase.FINISHED, vm.uiState.value.phase)
        // Máximo 2000 ms, no la suma secuencial (2000 + 1000).
        assertEquals(2000L, currentTime)
    }

    @Test fun pausaDetieneAmbosYReanudaDesdeElValorAnterior() = runTest(dispatcherRule.dispatcher) {
        val vm = CatalogSyncViewModel()
        vm.startOrResume()
        runCurrent()
        advanceTimeBy(400)
        runCurrent()
        assertEquals(20, vm.uiState.value.catalogProgress)
        assertEquals(40, vm.uiState.value.stockProgress)
        vm.pause()
        advanceTimeBy(1000)
        runCurrent()
        assertEquals(20, vm.uiState.value.catalogProgress)
        assertEquals(40, vm.uiState.value.stockProgress)
        assertEquals(SyncPhase.PAUSED, vm.uiState.value.phase)
        vm.startOrResume()
        advanceUntilIdle()
        assertEquals(SyncPhase.FINISHED, vm.uiState.value.phase)
        assertEquals(100, vm.uiState.value.catalogProgress)
        assertEquals(100, vm.uiState.value.stockProgress)
    }

    @Test fun reiniciarCancelaOperacionesYLimpiaLasBarras() = runTest(dispatcherRule.dispatcher) {
        val vm = CatalogSyncViewModel()
        vm.startOrResume()
        runCurrent()
        advanceTimeBy(300)
        runCurrent()
        assertTrue(vm.uiState.value.stockProgress > 0)
        vm.reset()
        advanceUntilIdle()
        assertEquals(CatalogSyncUiState(), vm.uiState.value)
    }

    @Test fun inicioDuplicadoNoLanzaUnaSegundaCarrera() = runTest(dispatcherRule.dispatcher) {
        val vm = CatalogSyncViewModel()
        vm.startOrResume()
        vm.startOrResume()
        advanceUntilIdle()
        assertEquals(2000L, currentTime)
        assertEquals(SyncPhase.FINISHED, vm.uiState.value.phase)
    }

    @Test fun unErrorEnUnaHijaCancelaLaOtra() = runTest(dispatcherRule.dispatcher) {
        var siblingCancelled = false
        val failing = object : SyncProgressTask {
            override suspend fun run(fromProgress: Int, onProgress: (Int)->Unit) {
                delay(100)
                error("error de prueba")
            }
        }
        val sibling = object : SyncProgressTask {
            override suspend fun run(fromProgress: Int, onProgress: (Int)->Unit) {
                try { awaitCancellation() } finally { siblingCancelled = true }
            }
        }
        val vm = CatalogSyncViewModel(DualCatalogSyncRunner(failing, sibling))
        vm.startOrResume()
        advanceUntilIdle()
        assertEquals(SyncPhase.ERROR, vm.uiState.value.phase)
        assertTrue(siblingCancelled)
    }

    @Test fun elUltimoIncrementoNoSuperaCien() = runTest(dispatcherRule.dispatcher) {
        val task = SteppedSyncProgressTask(37, 100)
        val values = mutableListOf<Int>()
        launch { task.run(0) { values += it } }
        advanceUntilIdle()
        assertEquals(listOf(37,74,100), values)
    }
}
