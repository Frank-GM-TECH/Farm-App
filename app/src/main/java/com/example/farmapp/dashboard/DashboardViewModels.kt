package com.example.farmapp.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.farmapp.data.catalog.CatalogProductEntity
import com.example.farmapp.data.catalog.CatalogRealtimeSync
import com.example.farmapp.data.farmAppContainer
import com.example.farmapp.data.orders.FarmOrder
import com.example.farmapp.data.orders.OrderRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class CatalogDashboardState(
    val products: List<CatalogProductEntity> = emptyList(),
    val isLoading: Boolean = true,
    val synchronizedAtMillis: Long? = null,
    val errorMessage: String? = null
)

/**
 * Consume el sincronizador Firestore -> Room que ya inicia FarmAppApplication.
 * No representa como reales los productos de semilla local antes de una lectura
 * confirmada del servidor para la sesión actual.
 */
class CatalogDashboardViewModel(
    private val sync: CatalogRealtimeSync
) : ViewModel() {
    private val _uiState = MutableStateFlow(CatalogDashboardState())
    val uiState: StateFlow<CatalogDashboardState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                sync.verifiedSnapshot.collect { snapshot ->
                    _uiState.value = if (snapshot == null) {
                        CatalogDashboardState(
                            isLoading = false,
                            errorMessage = "Sin datos verificados de Firestore. Conéctate a Internet y espera la sincronización del catálogo."
                        )
                    } else {
                        CatalogDashboardState(
                            products = snapshot.products,
                            isLoading = false,
                            synchronizedAtMillis = snapshot.synchronizedAtMillis
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _uiState.value = CatalogDashboardState(
                    isLoading = false,
                    errorMessage = "No se pudo consultar la sincronización del catálogo."
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CatalogDashboardViewModel(
                    this.farmAppContainer().catalogRealtimeSync
                )
            }
        }
    }
}

data class OrdersDashboardState(
    val orders: List<FarmOrder> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/** Lee solo users/{uid}/orders desde el servidor, mediante el repositorio existente. */
class OrdersDashboardViewModel(
    private val repository: OrderRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(OrdersDashboardState())
    val uiState: StateFlow<OrdersDashboardState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        _uiState.value = OrdersDashboardState(isLoading = true)
        viewModelScope.launch {
            try {
                val orders = repository.getOrdersFromServer()
                _uiState.value = OrdersDashboardState(orders = orders)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _uiState.value = OrdersDashboardState(
                    errorMessage = "No se pudieron consultar tus pedidos en Firestore. Comprueba tu conexión y vuelve a intentarlo."
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                OrdersDashboardViewModel(
                    this.farmAppContainer().orderRepository
                )
            }
        }
    }
}
