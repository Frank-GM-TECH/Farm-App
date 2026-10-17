package com.example.farmapp.data.room

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
import kotlinx.coroutines.launch

enum class RoomDemoMessage { NONE, SAVED, UPDATED, DELETED, INVALID, ERROR, SOLD, NO_STOCK, NOT_FOUND }

data class RoomDemoUiState(
    val items: List<RoomDemoItem> = emptyList(),
    val busy: Boolean = false,
    val message: RoomDemoMessage = RoomDemoMessage.NONE
)

class RoomDemoViewModel(private val repository: RoomDemoRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(RoomDemoUiState())
    val uiState: StateFlow<RoomDemoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.items.collect { latest ->
                    _uiState.value = _uiState.value.copy(items = latest)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(busy = false, message = RoomDemoMessage.ERROR)
            }
        }
    }

    fun save(name: String, price: String, quantity: String) {
        val parsed = RoomDemoForm.validate(name, price, quantity)
        if (parsed == null) {
            _uiState.value = _uiState.value.copy(message = RoomDemoMessage.INVALID)
            return
        }
        execute(RoomDemoMessage.SAVED) {
            repository.add(RoomDemoItem(name = parsed.name, priceCents = parsed.priceCents, stock = parsed.stock))
        }
    }

    fun increase(id: Long) = execute(RoomDemoMessage.UPDATED) { repository.increaseStock(id) }
    fun remove(item: RoomDemoItem) = execute(RoomDemoMessage.DELETED) { repository.remove(item) }

    private fun execute(success: RoomDemoMessage, operation: suspend () -> Unit) {
        if (_uiState.value.busy) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(busy = true, message = RoomDemoMessage.NONE)
            try {
                operation()
                _uiState.value = _uiState.value.copy(busy = false, message = success)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(busy = false, message = RoomDemoMessage.ERROR)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { RoomDemoViewModel(this.farmAppContainer().roomDemoRepository) }
        }
    }
}
