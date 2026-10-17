package com.example.farmapp.catalog

import androidx.lifecycle.ViewModel
import com.example.farmapp.data.farmAppContainer
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

enum class AvailabilityPhase { IDLE, LOADING, SUCCESS, CANCELLED, ERROR }

data class AvailabilityUiState(
    val phase: AvailabilityPhase = AvailabilityPhase.IDLE,
    val summary: DemoAvailabilitySummary? = null
)

/**
 * C19. ViewModel controla un Job estructurado dentro de viewModelScope.
 * Al abandonar la ruta y destruir el ViewModel, su Job se cancela automáticamente.
 * No creamos un CoroutineScope global que pueda sobrevivir indefinidamente a la pantalla.
 */
class CatalogAvailabilityViewModel(
    private val loader: CatalogAvailabilityLoader
) : ViewModel() {
    private val _uiState = MutableStateFlow(AvailabilityUiState())
    val uiState: StateFlow<AvailabilityUiState> = _uiState.asStateFlow()

    private var activeJob: Job? = null

    fun refresh() {
        // Caso límite: impedir consultas duplicadas mientras la anterior está activa.
        if (activeJob?.isActive == true) return
        _uiState.value = AvailabilityUiState(AvailabilityPhase.LOADING)
        activeJob = viewModelScope.launch {
            try {
                val summary = loader.load() // suspend: permite otras tareas de UI.
                _uiState.value = AvailabilityUiState(AvailabilityPhase.SUCCESS, summary)
            } catch (cancelled: CancellationException) {
                // ¡No convertir cancelaciones en errores de negocio!
                throw cancelled
            } catch (failure: Exception) {
                // No se muestran detalles internos de excepciones en la interfaz.
                _uiState.value = AvailabilityUiState(AvailabilityPhase.ERROR)
            }
        }
    }

    fun cancelRefresh() {
        if (activeJob?.isActive != true) return
        activeJob?.cancel() // Cancela también los trabajos hijos lanzados mediante async.
        activeJob = null
        _uiState.value = AvailabilityUiState(AvailabilityPhase.CANCELLED)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CatalogAvailabilityViewModel(
                    CatalogAvailabilityLoader(
                        LocalDemoAvailabilitySource(this.farmAppContainer().catalogRepository)
                    )
                )
            }
        }
    }
}
