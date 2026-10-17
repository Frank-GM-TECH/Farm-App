package com.example.farmapp.network

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
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

enum class RemoteProductsPhase { IDLE, LOADING, SUCCESS, EMPTY, ERROR, RAW_SUCCESS }
enum class RemoteProductsError { NETWORK, HTTP, DATA, UNKNOWN }

data class RemoteProductsUiState(
    val phase: RemoteProductsPhase = RemoteProductsPhase.IDLE,
    val products: List<RemoteProduct> = emptyList(),
    val error: RemoteProductsError? = null,
    val rawJsonPreview: String? = null
)

/** C21: las solicitudes HTTP son suspendibles y cancelables con el ViewModel. */
class RemoteProductsViewModel(
    private val repository: RemoteProductsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RemoteProductsUiState())
    val uiState: StateFlow<RemoteProductsUiState> = _uiState.asStateFlow()
    private var request: Job? = null

    fun refresh() {
        if (request?.isActive == true) return
        _uiState.value = RemoteProductsUiState(RemoteProductsPhase.LOADING)
        request = viewModelScope.launch {
            try {
                val products = repository.loadSkinCare()
                _uiState.value = RemoteProductsUiState(
                    phase = if (products.isEmpty()) RemoteProductsPhase.EMPTY
                            else RemoteProductsPhase.SUCCESS,
                    products = products
                )
            } catch (cancelled: CancellationException) {
                throw cancelled // La cancelación NO debe convertirse en error visual.
            } catch (error: HttpException) {
                _uiState.value = RemoteProductsUiState(
                    RemoteProductsPhase.ERROR, error = RemoteProductsError.HTTP
                )
            } catch (error: IOException) {
                _uiState.value = RemoteProductsUiState(
                    RemoteProductsPhase.ERROR, error = RemoteProductsError.NETWORK
                )
            } catch (error: SerializationException) {
                _uiState.value = RemoteProductsUiState(
                    RemoteProductsPhase.ERROR, error = RemoteProductsError.DATA
                )
            } catch (error: Exception) {
                _uiState.value = RemoteProductsUiState(
                    RemoteProductsPhase.ERROR, error = RemoteProductsError.UNKNOWN
                )
            }
        }
    }

    /** Etapa "ScalarsConverter": leer JSON bruto y mostrar solo una muestra. */
    fun previewJson() {
        if (request?.isActive == true) return
        _uiState.value = RemoteProductsUiState(RemoteProductsPhase.LOADING)
        request = viewModelScope.launch {
            try {
                val response = repository.previewRawJson()
                _uiState.value = RemoteProductsUiState(
                    phase = RemoteProductsPhase.RAW_SUCCESS,
                    rawJsonPreview = response.take(700) // No saturar la interfaz.
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: HttpException) {
                _uiState.value = RemoteProductsUiState(
                    RemoteProductsPhase.ERROR, error = RemoteProductsError.HTTP
                )
            } catch (error: IOException) {
                _uiState.value = RemoteProductsUiState(
                    RemoteProductsPhase.ERROR, error = RemoteProductsError.NETWORK
                )
            } catch (error: Exception) {
                _uiState.value = RemoteProductsUiState(
                    RemoteProductsPhase.ERROR, error = RemoteProductsError.UNKNOWN
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RemoteProductsViewModel(
                    repository = this.farmAppContainer().remoteProductsRepository
                )
            }
        }
    }
}
