package com.example.farmapp.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SyncPhase { IDLE, RUNNING, PAUSED, FINISHED, ERROR }

data class CatalogSyncUiState(
    val catalogProgress: Int = 0,
    val stockProgress: Int = 0,
    val phase: SyncPhase = SyncPhase.IDLE
)

/** C20: ViewModel posee un único Job padre; ambos progresos son tareas hijas. */
class CatalogSyncViewModel(
    private val runner: DualCatalogSyncRunner = DualCatalogSyncRunner()
) : ViewModel() {
    private val _uiState = MutableStateFlow(CatalogSyncUiState())
    val uiState: StateFlow<CatalogSyncUiState> = _uiState.asStateFlow()
    private var activeJob: Job? = null

    fun startOrResume() {
        if (activeJob?.isActive == true || _uiState.value.phase == SyncPhase.FINISHED) return
        val previous = _uiState.value
        _uiState.value = previous.copy(phase = SyncPhase.RUNNING)
        activeJob = viewModelScope.launch {
            try {
                runner.run(
                    catalogFrom = previous.catalogProgress,
                    stockFrom = previous.stockProgress,
                    onCatalogProgress = { value ->
                        _uiState.value = _uiState.value.copy(catalogProgress = value)
                    },
                    onStockProgress = { value ->
                        _uiState.value = _uiState.value.copy(stockProgress = value)
                    }
                )
                _uiState.value = _uiState.value.copy(phase = SyncPhase.FINISHED)
            } catch (cancelled: CancellationException) {
                // Pausar o reiniciar cancela las dos hijas. No confundir con fallo.
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(phase = SyncPhase.ERROR)
            }
        }
    }

    fun pause() {
        if (_uiState.value.phase != SyncPhase.RUNNING) return
        activeJob?.cancel()
        activeJob = null
        _uiState.value = _uiState.value.copy(phase = SyncPhase.PAUSED)
    }

    fun reset() {
        activeJob?.cancel()
        activeJob = null
        _uiState.value = CatalogSyncUiState()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { CatalogSyncViewModel() }
        }
    }
}
