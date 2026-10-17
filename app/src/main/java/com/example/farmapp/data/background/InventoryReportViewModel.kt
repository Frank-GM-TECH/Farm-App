package com.example.farmapp.data.background

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.farmapp.data.farmAppContainer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class InventoryReportViewModel(private val repository: InventoryReportRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportWorkUiState())
    val uiState: StateFlow<ReportWorkUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.status.collect { _uiState.value = it }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _uiState.value = ReportWorkUiState(ReportPhase.FAILED)
            }
        }
    }

    fun start() = repository.enqueueReport()
    fun cancel() = repository.cancelReport()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { InventoryReportViewModel(this.farmAppContainer().inventoryReportRepository) }
        }
    }
}
