package com.example.farmapp.data.preferences

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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class UserPreferencesUiState(
    val preferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false
)

/** C27: ViewModel mantiene estado observable; persistencia fuera de Compose. */
class UserPreferencesViewModel(
    private val repository: UserPreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserPreferencesUiState())
    val uiState: StateFlow<UserPreferencesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.preferences
                .catch { cause ->
                    if (cause is CancellationException) throw cause
                    _uiState.value = _uiState.value.copy(isLoading = false, hasError = true)
                }
                .collect { preferences ->
                    _uiState.value = UserPreferencesUiState(preferences, isLoading = false)
                }
        }
    }

    fun setTheme(theme: AppThemePreference) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            try {
                repository.setTheme(theme)
            } catch (cause: CancellationException) {
                throw cause
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(hasError = true)
            }
        }
    }

    fun setGalleryLayout(layout: GalleryLayoutPreference) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            try {
                repository.setGalleryLayout(layout)
            } catch (cause: CancellationException) {
                throw cause
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(hasError = true)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                UserPreferencesViewModel(this.farmAppContainer().userPreferencesRepository)
            }
        }
    }
}
