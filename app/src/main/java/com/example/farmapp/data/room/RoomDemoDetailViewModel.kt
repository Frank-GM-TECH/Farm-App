package com.example.farmapp.data.room

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.createSavedStateHandle
import com.example.farmapp.data.farmAppContainer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** El único argumento de navegación es el ID, nunca un objeto completo. */
data class RoomDemoDetailState(
    val item: RoomDemoItem? = null,
    val loaded: Boolean = false,
    val busy: Boolean = false,
    val feedback: RoomDemoMessage = RoomDemoMessage.NONE
)

class RoomDemoDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: RoomDemoRepository
) : ViewModel() {
    val itemId: Long = requireNotNull(savedStateHandle.get<Long>("itemId"))
    private val _state = MutableStateFlow(RoomDemoDetailState())
    val state: StateFlow<RoomDemoDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.observeItem(itemId).collect { item ->
                    _state.value = _state.value.copy(item = item, loaded = true)
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { _state.value = _state.value.copy(loaded = true, feedback = RoomDemoMessage.ERROR) }
        }
    }

    fun sellOne() = execute({
        repository.sellOne(itemId)
    }, success = RoomDemoMessage.SOLD, failure = RoomDemoMessage.NO_STOCK)

    fun update(name: String, price: String, stock: String) {
        val valid = RoomDemoForm.validate(name, price, stock)
        if (valid == null) {
            _state.value = _state.value.copy(feedback = RoomDemoMessage.INVALID)
            return
        }
        execute({ repository.updateRecord(itemId, valid.name, valid.priceCents, valid.stock) },
            success = RoomDemoMessage.UPDATED, failure = RoomDemoMessage.NOT_FOUND)
    }

    fun delete() = execute({ repository.deleteRecord(itemId) },
        success = RoomDemoMessage.DELETED, failure = RoomDemoMessage.NOT_FOUND)

    private fun execute(
        task: suspend () -> Boolean,
        success: RoomDemoMessage,
        failure: RoomDemoMessage
    ) {
        if (_state.value.busy) return
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, feedback = RoomDemoMessage.NONE)
            try {
                val ok = task()
                _state.value = _state.value.copy(busy = false, feedback = if (ok) success else failure)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { _state.value = _state.value.copy(busy = false, feedback = RoomDemoMessage.ERROR) }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RoomDemoDetailViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = this.farmAppContainer().roomDemoRepository
                )
            }
        }
    }
}
