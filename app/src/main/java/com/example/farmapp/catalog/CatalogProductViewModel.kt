
package com.example.farmapp.catalog

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.farmapp.data.ai.GeminiQueryInterpreter
import com.example.farmapp.data.catalog.CatalogProductEntity
import com.example.farmapp.data.catalog.CatalogProductRepository
import com.example.farmapp.data.farmAppContainer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * HU03 - Estado del catálogo persistente.
 */
data class CatalogProductUiState(
    val products: List<CatalogProductEntity> = emptyList(),
    val categories: List<String> = emptyList(),
    val query: String = "",
    val selectedCategory: String? = null,
    val selectedProduct: CatalogProductEntity? = null,
    val isLoading: Boolean = true,
    val isInterpretingVoice: Boolean = false,
    val errorMessage: String? = null
)

/**
 * HU03 - ViewModel del catálogo de FarmApp.
 *
 * Room realiza las búsquedas de productos.
 * Gemini interpreta las consultas recibidas por voz.
 */
class CatalogProductViewModel(
    private val repository: CatalogProductRepository,
    private val queryInterpreter: GeminiQueryInterpreter =
        GeminiQueryInterpreter()
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    private val categoryFilter = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(CatalogProductUiState())

    val uiState: StateFlow<CatalogProductUiState> = _uiState.asStateFlow()

    private var voiceSearchJob: Job? = null

    init {
        loadProducts()
    }

    // =========================================
    // CARGAR PRODUCTOS DESDE ROOM
    // =========================================

    private fun loadProducts() {
        viewModelScope.launch {
            try {
                repository.initializeSampleProducts()

                combine(
                    repository.products,
                    searchQuery,
                    categoryFilter
                ) { allProducts, query, category ->

                    val availableCategories = allProducts
                        .map { it.category }
                        .distinct()
                        .sorted()

                    val searchText = query.trim()

                    val filteredProducts = allProducts
                        .filter { product ->

                            val matchesQuery =
                                searchText.isBlank() ||
                                        product.name.contains(
                                            searchText,
                                            ignoreCase = true
                                        ) ||
                                        product.sku.contains(
                                            searchText,
                                            ignoreCase = true
                                        ) ||
                                        (product.barcode?.contains(
                                            searchText,
                                            ignoreCase = true
                                        ) == true)

                            val matchesCategory =
                                category == null ||
                                        product.category == category

                            matchesQuery && matchesCategory
                        }
                        .sortedBy {
                            it.name.lowercase()
                        }

                    CatalogProductUiState(
                        products = filteredProducts,
                        categories = availableCategories,
                        query = query,
                        selectedCategory = category,
                        isLoading = false
                    )

                }.collect { updatedState ->

                    _uiState.update { current ->
                        updatedState.copy(
                            selectedProduct = current.selectedProduct,
                            errorMessage = current.errorMessage,
                            isInterpretingVoice = current.isInterpretingVoice
                        )
                    }
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                Log.e(
                    "FarmAppCatalog",
                    "Error al cargar productos",
                    e
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo cargar el catálogo."
                    )
                }
            }
        }
    }

    // =========================================
    // ACTUALIZAR CONSULTA DE BÚSQUEDA
    // =========================================

    private fun updateSearchQuery(
        query: String,
        interpretingVoice: Boolean = false
    ) {
        searchQuery.value = query

        _uiState.update {
            it.copy(
                query = query,
                errorMessage = null,
                selectedProduct = null,
                isInterpretingVoice = interpretingVoice
            )
        }
    }

    // =========================================
    // BUSCAR POR TEXTO ESCRITO
    // =========================================

    fun onQueryChanged(query: String) {
        voiceSearchJob?.cancel()
        voiceSearchJob = null

        updateSearchQuery(
            query = query,
            interpretingVoice = false
        )
    }

    // =========================================
    // BUSCAR POR VOZ CON GEMINI
    // =========================================

    fun onVoiceQueryReceived(spokenText: String) {

        val originalQuery = spokenText.trim()

        if (originalQuery.isBlank()) return

        voiceSearchJob?.cancel()
        voiceSearchJob = null

        Log.d("FarmAppVoiceAI", "Consulta reconocida: $originalQuery")

        updateSearchQuery(
            query = originalQuery,
            interpretingVoice = true
        )

        voiceSearchJob = viewModelScope.launch {
            try {

                Log.d("FarmAppVoiceAI", "Enviando consulta a Gemini...")

                val response = withTimeoutOrNull(20_000L) {
                    queryInterpreter.interpret(originalQuery)
                }

                val interpretedQuery = response
                    ?.trim()
                    ?.ifBlank { originalQuery }
                    ?: originalQuery

                if (response == null) {
                    Log.e(
                        "FarmAppVoiceAI",
                        "Tiempo agotado: Gemini no respondió en 20 segundos"
                    )
                } else {
                    Log.d(
                        "FarmAppVoiceAI",
                        "Gemini respondió: $interpretedQuery"
                    )
                }

                if (searchQuery.value == originalQuery) {
                    updateSearchQuery(
                        query = interpretedQuery,
                        interpretingVoice = false
                    )

                    Log.d(
                        "FarmAppVoiceAI",
                        "Búsqueda actualizada en Room: $interpretedQuery"
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                Log.e(
                    "FarmAppVoiceAI",
                    "Error de Gemini: ${e.message}",
                    e
                )

                if (searchQuery.value == originalQuery) {
                    updateSearchQuery(
                        query = originalQuery,
                        interpretingVoice = false
                    )

                    Log.w(
                        "FarmAppVoiceAI",
                        "Se conserva la búsqueda original: $originalQuery"
                    )
                }
            }
        }

    }

    // =========================================
    // FILTRAR POR CATEGORÍA
    // =========================================

    fun onCategorySelected(category: String?) {
        categoryFilter.value = category

        _uiState.update {
            it.copy(
                errorMessage = null
            )
        }
    }

    // =========================================
    // BUSCAR PRODUCTO POR CÓDIGO DE BARRAS
    // =========================================

    fun onBarcodeScanned(barcode: String) {

        voiceSearchJob?.cancel()
        voiceSearchJob = null

        val normalizedBarcode = barcode.trim()

        if (normalizedBarcode.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage = "El código de barras está vacío.",
                    isInterpretingVoice = false
                )
            }

            return
        }

        viewModelScope.launch {
            try {
                val product = repository.findByBarcode(
                    normalizedBarcode
                )

                if (product != null) {

                    categoryFilter.value = null

                    updateSearchQuery(
                        query = normalizedBarcode
                    )

                    _uiState.update {
                        it.copy(
                            selectedProduct = product,
                            errorMessage = null
                        )
                    }

                } else {
                    _uiState.update {
                        it.copy(
                            selectedProduct = null,
                            errorMessage =
                                "No se encontró un producto " +
                                        "con ese código de barras.",
                            isInterpretingVoice = false
                        )
                    }
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                Log.e(
                    "FarmAppCatalog",
                    "Error al buscar código de barras",
                    e
                )

                _uiState.update {
                    it.copy(
                        errorMessage = "No se pudo buscar el producto."
                    )
                }
            }
        }
    }

    // =========================================
    // SELECCIONAR PRODUCTO
    // =========================================

    fun onProductSelected(product: CatalogProductEntity) {
        _uiState.update {
            it.copy(
                selectedProduct = product
            )
        }
    }

    // =========================================
    // LIMPIAR MENSAJE
    // =========================================

    fun clearMessage() {
        _uiState.update {
            it.copy(
                errorMessage = null
            )
        }
    }

    // =========================================
    // FACTORY - INYECCIÓN DE DEPENDENCIAS
    // =========================================

    companion object {

        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    CatalogProductViewModel(
                        repository = this.farmAppContainer()
                            .catalogProductRepository
                    )
                }
            }
    }
}
