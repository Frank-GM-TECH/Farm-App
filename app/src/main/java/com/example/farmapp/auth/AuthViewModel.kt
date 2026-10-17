package com.example.farmapp.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.farmapp.R
import com.example.farmapp.data.farmAppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

/** Lógica de la pantalla: recibe eventos y publica el estado mediante StateFlow. */
class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            isAuthenticated = repository.isAuthenticated(),
            userEmail = repository.currentUserEmail()
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) = authenticate(email, password, register = false)

    fun register(email: String, password: String) = authenticate(email, password, register = true)

    private fun authenticate(email: String, password: String, register: Boolean) {
        val emailDraft = EmailDraft(email)
        val cleanEmail = emailDraft.value
        if (_uiState.value.isLoading) return

        if (!emailDraft.isValid) {
            showError(R.string.auth_error_invalid_email)
            return
        }
        if (!AuthValidator.isValidPassword(password)) {
            showError(R.string.auth_error_password_short)
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessageRes = null, infoMessageRes = null)
            }
            try {
                if (register) {
                    repository.register(cleanEmail, password)
                } else {
                    repository.signIn(cleanEmail, password)
                }
                _uiState.update {
                    it.copy(
                        isAuthenticated = true,
                        userEmail = repository.currentUserEmail(),
                        isLoading = false,
                        errorMessageRes = null
                    )
                }
            } catch (cancelled: CancellationException) {
                // Concurrencia estructurada: la cancelación se propaga al llamador.
                throw cancelled
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessageRes = R.string.auth_error_sign_in
                    )
                }
            }
        }
    }

    /** HU01: intercambio seguro del ID token con Firebase. El selector de cuenta vive en UI. */
    fun signInWithGoogleIdToken(idToken: String) {
        if (_uiState.value.isLoading) return
        if (idToken.isBlank()) {
            showError(R.string.auth_error_google)
            return
        }
        // Actualizar antes de lanzar evita dobles taps mientras arranca la corrutina.
        _uiState.update { it.copy(isLoading = true, errorMessageRes = null, infoMessageRes = null) }
        viewModelScope.launch {
            try {
                repository.signInWithGoogleIdToken(idToken)
                _uiState.update { it.copy(
                    isAuthenticated = repository.isAuthenticated(),
                    userEmail = repository.currentUserEmail(),
                    isLoading = false,
                    errorMessageRes = null
                ) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.update { it.copy(isLoading = false,
                    errorMessageRes = R.string.auth_error_google) }
            }
        }
    }

    /** Errores del selector Google; nunca registrar tokens ni datos privados. */
    fun reportGoogleCredentialError(configurationMissing: Boolean = false) {
        if (_uiState.value.isLoading) return
        showError(if (configurationMissing) R.string.auth_error_google_setup
            else R.string.auth_error_google)
    }

    fun resetPassword(email: String) {
        val emailDraft = EmailDraft(email)
        val cleanEmail = emailDraft.value
        if (_uiState.value.isLoading) return
        if (!emailDraft.isValid) {
            showError(R.string.auth_error_reset_email)
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessageRes = null, infoMessageRes = null)
            }
            try {
                repository.resetPassword(cleanEmail)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        infoMessageRes = R.string.auth_info_password_reset
                    )
                }
            } catch (cancelled: CancellationException) {
                // Concurrencia estructurada: la cancelación se propaga al llamador.
                throw cancelled
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessageRes = R.string.auth_error_reset_request
                    )
                }
            }
        }
    }

    fun signOut() {
        repository.signOut()
        _uiState.value = AuthUiState()
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessageRes = null, infoMessageRes = null) }
    }

    private fun showError(messageRes: Int) {
        _uiState.update { it.copy(errorMessageRes = messageRes, infoMessageRes = null) }
    }

    companion object {
        /** C22: el repositorio proviene del contenedor de Application. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AuthViewModel(repository = this.farmAppContainer().authRepository)
            }
        }
    }
}
