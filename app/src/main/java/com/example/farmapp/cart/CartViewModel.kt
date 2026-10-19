package com.example.farmapp.cart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.farmapp.data.cart.CartItem
import com.example.farmapp.data.cart.CartRepository
import com.example.farmapp.data.farmAppContainer
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * HU04 - Estado de la interfaz del carrito.
 *
 * Los montos se almacenan en centimos.
 */
data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val totalUnits: Int = 0,
    val subtotalCents: Long = 0L,
    val isLoading: Boolean = true,
    val isWorking: Boolean = false,
    val message: String? = null
)

/**
 * HU04 - ViewModel del carrito de FarmApp.
 *
 * - Observa productos desde Room.
 * - Utiliza el UID de Firebase Authentication.
 * - Agrega y elimina productos.
 * - Actualiza cantidades.
 * - Calcula subtotales.
 * - Limpia el estado al cambiar de usuario.
 */
class CartViewModel(
    private val repository: CartRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val activeUserId =
        MutableStateFlow(auth.currentUser?.uid)

    private val _uiState =
        MutableStateFlow(CartUiState())

    val uiState: StateFlow<CartUiState> =
        _uiState.asStateFlow()

    /**
     * Detectar inicio y cierre de sesion para
     * evitar mostrar datos de una cuenta anterior.
     */
    private val authListener =
        FirebaseAuth.AuthStateListener { firebaseAuth ->
            activeUserId.value =
                firebaseAuth.currentUser?.uid
        }

    init {
        auth.addAuthStateListener(authListener)
        observeCurrentUserCart()
    }

    // =========================================
    // OBSERVAR CARRITO CON ROOM
    // =========================================

    private fun observeCurrentUserCart() {

        viewModelScope.launch {

            activeUserId.collectLatest { userId ->

                // Borrar el estado visible cuando no
                // exista una sesion autenticada.
                if (userId == null) {
                    _uiState.value = CartUiState(
                        isLoading = false
                    )

                    return@collectLatest
                }

                // Evita mostrar datos anteriores
                // mientras se carga otra cuenta.
                _uiState.value = CartUiState(
                    isLoading = true
                )

                try {
                    repository.observeCart(userId)
                        .collect { items ->

                            if (activeUserId.value != userId) {
                                return@collect
                            }

                            _uiState.update { current ->

                                current.copy(
                                    items = items,

                                    totalUnits = items.sumOf {
                                        it.quantity
                                    },

                                    subtotalCents = items.sumOf {
                                        it.subtotalCents
                                    },

                                    isLoading = false
                                )
                            }
                        }

                } catch (e: CancellationException) {
                    throw e

                } catch (e: Exception) {

                    Log.e(
                        "FarmAppCart",
                        "Error al observar el carrito",
                        e
                    )

                    if (activeUserId.value == userId) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                message =
                                    "No se pudo cargar el carrito."
                            )
                        }
                    }
                }
            }
        }
    }

    // =========================================
    // AGREGAR PRODUCTO
    // =========================================

    fun addProduct(
        sku: String,
        quantity: Int = 1
    ) {

        if (sku.isBlank() || quantity <= 0) {
            showMessage("Producto o cantidad inválida.")
            return
        }

        executeAction { userId ->
            repository.addProduct(
                userId = userId,
                sku = sku,
                quantity = quantity
            )
        }
    }

    // =========================================
    // CAMBIAR CANTIDAD
    // =========================================

    fun changeQuantity(
        sku: String,
        quantity: Int
    ) {

        if (sku.isBlank() || quantity < 0) {
            showMessage("Cantidad inválida.")
            return
        }

        executeAction { userId ->
            repository.changeQuantity(
                userId = userId,
                sku = sku,
                quantity = quantity
            )
        }
    }

    // =========================================
    // ELIMINAR PRODUCTO
    // =========================================

    fun removeProduct(sku: String) {

        if (sku.isBlank()) {
            return
        }

        executeAction { userId ->

            repository.removeProduct(
                userId = userId,
                sku = sku
            )

            true
        }
    }

    // =========================================
    // VACIAR CARRITO
    // =========================================

    fun clearCart() {

        executeAction { userId ->

            repository.clearCart(userId)

            true
        }
    }

    // =========================================
    // EJECUTAR OPERACIONES SEGURAS
    // =========================================

    private fun executeAction(
        action: suspend (String) -> Boolean
    ) {

        val userId = activeUserId.value

        if (userId == null) {
            showMessage(
                "Debes iniciar sesión para utilizar el carrito."
            )
            return
        }

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isWorking = true,
                    message = null
                )
            }

            try {

                val success = action(userId)

                if (activeUserId.value == userId) {

                    _uiState.update {
                        it.copy(
                            isWorking = false,

                            message =
                                if (success) null
                                else "No se pudo actualizar el carrito. " +
                                        "Revisa la disponibilidad del producto."
                        )
                    }
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                Log.e(
                    "FarmAppCart",
                    "Error al modificar el carrito",
                    e
                )

                if (activeUserId.value == userId) {

                    _uiState.update {
                        it.copy(
                            isWorking = false,
                            message =
                                "Ocurrió un error al actualizar el carrito."
                        )
                    }
                }
            }
        }
    }

    // =========================================
    // MENSAJES DE LA INTERFAZ
    // =========================================

    private fun showMessage(message: String) {

        _uiState.update {
            it.copy(message = message)
        }
    }

    fun clearMessage() {

        _uiState.update {
            it.copy(message = null)
        }
    }

    // =========================================
    // LIBERAR RECURSOS
    // =========================================

    override fun onCleared() {

        auth.removeAuthStateListener(authListener)

        super.onCleared()
    }

    // =========================================
    // INYECCION MANUAL DE DEPENDENCIAS
    // =========================================

    companion object {

        val Factory: ViewModelProvider.Factory =
            viewModelFactory {

                initializer {

                    CartViewModel(
                        repository =
                            this.farmAppContainer()
                                .cartRepository,

                        auth = FirebaseAuth.getInstance()
                    )
                }
            }
    }
}