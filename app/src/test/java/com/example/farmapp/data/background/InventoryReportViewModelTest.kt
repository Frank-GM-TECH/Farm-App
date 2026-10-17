package com.example.farmapp.data.background

import com.example.farmapp.auth.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryReportViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private class FakeRepository : InventoryReportRepository {
        override val status = MutableStateFlow(ReportWorkUiState())
        var starts = 0
        var stops = 0
        override fun enqueueReport() { starts++ }
        override fun cancelReport() { stops++ }
    }

    @Test fun exito_viewModelObservaEstadosDeRepositorio() = runTest(main.dispatcher) {
        val repo = FakeRepository()
        val vm = InventoryReportViewModel(repo)
        advanceUntilIdle()
        repo.status.value = ReportWorkUiState(ReportPhase.SAVING)
        advanceUntilIdle()
        assertEquals(ReportPhase.SAVING, vm.uiState.value.phase)
    }
    @Test fun exito_botonesDeleganEnRepositorio() = runTest(main.dispatcher) {
        val repo = FakeRepository()
        val vm = InventoryReportViewModel(repo)
        vm.start()
        vm.cancel()
        assertEquals(1, repo.starts)
        assertEquals(1, repo.stops)
    }
    @Test fun limite_persisteNombreDelArchivoEnEstadoObservado() = runTest(main.dispatcher) {
        val repo = FakeRepository()
        val vm = InventoryReportViewModel(repo)
        advanceUntilIdle()
        repo.status.value = ReportWorkUiState(ReportPhase.COMPLETED, "informe_demo.txt")
        advanceUntilIdle()
        assertEquals("informe_demo.txt", vm.uiState.value.fileName)
    }
}
