package com.example.farmapp.catalog

import com.example.farmapp.auth.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** C19: rutas exitosa, error, límite, cancelación y propagación de Job. */
@OptIn(ExperimentalCoroutinesApi::class)
class CatalogAvailabilityTest {
    @get:Rule val main = MainDispatcherRule()

    @Test fun exito_tresCategoriasSeConsultanEnParalelo() = runTest(main.dispatcher) {
        val source = object : DemoAvailabilitySource {
            override suspend fun unitsFor(category: DemoCategory): Int {
                delay(1000)
                return when (category) {
                    DemoCategory.HIGIENE -> 19
                    DemoCategory.PRIMEROS_AUXILIOS -> 7
                    DemoCategory.EQUIPOS -> 8
                }
            }
        }
        val loader = CatalogAvailabilityLoader(source, main.dispatcher)
        val request = async { loader.load() }
        runCurrent()
        advanceTimeBy(1000)
        runCurrent()
        assertTrue(request.isCompleted)
        assertEquals(34, request.await().totalUnits)
        // Si se ejecutara secuencialmente tardaría 3000 ms virtuales.
        assertEquals(1000L, currentTime)
    }

    @Test fun exito_localCoincideConInventarioFicticio() = runTest(main.dispatcher) {
        val loader = CatalogAvailabilityLoader(
            LocalDemoAvailabilitySource(DemoCatalogData.repository, simulatedLatencyMs = 0),
            main.dispatcher
        )
        val state = loader.load()
        assertEquals(34, state.totalUnits)
        assertEquals(19, state.unitsByCategory[DemoCategory.HIGIENE])
    }

    @Test fun viewModel_emiteCargaYResultado() = runTest(main.dispatcher) {
        val vm = CatalogAvailabilityViewModel(loader(main.dispatcher) { 5 })
        assertEquals(AvailabilityPhase.IDLE, vm.uiState.value.phase)
        vm.refresh()
        assertEquals(AvailabilityPhase.LOADING, vm.uiState.value.phase)
        advanceUntilIdle()
        assertEquals(AvailabilityPhase.SUCCESS, vm.uiState.value.phase)
        assertEquals(15, vm.uiState.value.summary?.totalUnits)
    }

    @Test fun error_repositorioFallaYSePuedeReintentar() = runTest(main.dispatcher) {
        var shouldFail = true
        val vm = CatalogAvailabilityViewModel(loader(main.dispatcher) {
            if (shouldFail) error("Fallo simulado") else 2
        })
        vm.refresh()
        advanceUntilIdle()
        assertEquals(AvailabilityPhase.ERROR, vm.uiState.value.phase)
        shouldFail = false
        vm.refresh()
        advanceUntilIdle()
        assertEquals(AvailabilityPhase.SUCCESS, vm.uiState.value.phase)
        assertEquals(6, vm.uiState.value.summary?.totalUnits)
    }

    @Test fun limite_consultaDuplicadaSeIgnora() = runTest(main.dispatcher) {
        var calls = 0
        val vm = CatalogAvailabilityViewModel(loader(main.dispatcher) {
            calls++
            delay(500)
            1
        })
        vm.refresh()
        runCurrent()
        vm.refresh()
        advanceUntilIdle()
        assertEquals(3, calls) // Una llamada por categoría, NO dos tandas.
        assertEquals(AvailabilityPhase.SUCCESS, vm.uiState.value.phase)
    }

    @Test fun cancelacion_interrumpeHijosYNoProduceError() = runTest(main.dispatcher) {
        var cancelledChildren = 0
        val loader = CatalogAvailabilityLoader(object : DemoAvailabilitySource {
            override suspend fun unitsFor(category: DemoCategory): Int = try {
                awaitCancellation()
            } finally {
                cancelledChildren++
            }
        }, main.dispatcher)
        val vm = CatalogAvailabilityViewModel(loader)
        vm.refresh()
        runCurrent()
        assertEquals(AvailabilityPhase.LOADING, vm.uiState.value.phase)
        vm.cancelRefresh()
        advanceUntilIdle()
        assertEquals(3, cancelledChildren)
        assertEquals(AvailabilityPhase.CANCELLED, vm.uiState.value.phase)
    }

    @Test fun error_deUnaCategoriaCancelaATodasLasHermanas() = runTest(main.dispatcher) {
        var cancelledSiblings = 0
        val loader = CatalogAvailabilityLoader(object : DemoAvailabilitySource {
            override suspend fun unitsFor(category: DemoCategory): Int = try {
                if (category == DemoCategory.HIGIENE) {
                    delay(10)
                    error("Fallo de una categoría")
                }
                awaitCancellation()
            } finally {
                if (category != DemoCategory.HIGIENE) cancelledSiblings++
            }
        }, main.dispatcher)
        val vm = CatalogAvailabilityViewModel(loader)
        vm.refresh()
        advanceUntilIdle()
        assertEquals(AvailabilityPhase.ERROR, vm.uiState.value.phase)
        assertEquals(2, cancelledSiblings)
    }

    private fun loader(
        dispatcher: kotlinx.coroutines.CoroutineDispatcher,
        fetch: suspend (DemoCategory) -> Int
    ) = CatalogAvailabilityLoader(object : DemoAvailabilitySource {
        override suspend fun unitsFor(category: DemoCategory) = fetch(category)
    }, dispatcher)
}
