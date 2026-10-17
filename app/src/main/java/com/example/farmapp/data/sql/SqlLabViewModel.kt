package com.example.farmapp.data.sql

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.farmapp.data.farmAppContainer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SqlLabUiState(
    val query: SqlLabQuery = SqlLabQuery.ALL,
    val result: SqlLabResult = SqlLabResult.Products(emptyList()),
    val loading: Boolean = false,
    val feedback: SqlLabFeedback = SqlLabFeedback.NONE,
    val hasError: Boolean = false
)

/** MVVM y DI manual, reutiliza C14, C19 y C22. */
class SqlLabViewModel(private val repository: SqlLabRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(SqlLabUiState())
    val uiState: StateFlow<SqlLabUiState> = _uiState.asStateFlow()
    private var request: Job? = null

    init { choose(SqlLabQuery.ALL) }

    fun choose(query: SqlLabQuery) {
        request?.cancel()
        _uiState.value = _uiState.value.copy(query = query, loading = true, hasError = false, feedback = SqlLabFeedback.NONE)
        request = viewModelScope.launch {
            try {
                val result = repository.runQuery(query)
                _uiState.value = _uiState.value.copy(result = result, loading = false)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(loading = false, hasError = true)
            }
        }
    }

    fun insert() = mutate { if (repository.insertExample()) SqlLabFeedback.INSERTED else SqlLabFeedback.EXISTS }
    fun update() = mutate { if (repository.updateExample() > 0) SqlLabFeedback.UPDATED else SqlLabFeedback.NOT_FOUND }
    fun delete() = mutate { if (repository.deleteExample() > 0) SqlLabFeedback.DELETED else SqlLabFeedback.NOT_FOUND }
    fun restore() = mutate { repository.resetExamples(); SqlLabFeedback.RESTORED }

    private fun mutate(action: suspend () -> SqlLabFeedback) {
        if (_uiState.value.loading) return
        request?.cancel()
        request = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, hasError = false, feedback = SqlLabFeedback.NONE)
            try {
                val feedback = action()
                val query = _uiState.value.query
                val result = repository.runQuery(query)
                _uiState.value = _uiState.value.copy(result = result, loading = false, feedback = feedback)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(loading = false, hasError = true)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { SqlLabViewModel(this.farmAppContainer().sqlLabRepository) }
        }
    }
}
