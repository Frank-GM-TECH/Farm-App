package com.example.farmapp.catalog

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.farmapp.data.catalog.CatalogProductEntity
import com.example.farmapp.data.catalog.CatalogProductRepository
import com.example.farmapp.data.farmAppContainer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val errorMessage: String? = null
)

/**
 * HU03 - ViewModel del catálogo de FarmApp.
 *
 * Los productos se obtienen de Room mediante
 * CatalogProductRepository.
 */
class CatalogProductViewModel(
    private val repository: CatalogProductRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    private val categoryFilter =
        MutableStateFlow<String?>(null)

    private val _uiState =
        MutableStateFlow(CatalogProductUiState())

    val uiState: StateFlow<CatalogProductUiState> =
        _uiState.asStateFlow()

    init {
        loadProducts()
    }

    // =========================================
    // CARGAR PRODUCTOS DESDE ROOM
    // =========================================

    private fun loadProducts() {

        viewModelScope.launch {

            try {

                // Inserta productos de prueba solo si
                // la tabla del catálogo está vacía.
                repository.initializeSampleProducts()

                // Observar productos y filtros.
                combine(
                    repository.products,
                    searchQuery,
                    categoryFilter
                ) { allProducts, query, category ->

                    val availableCategories =
                        allProducts
                            .map { it.category }
                            .distinct()
                            .sorted()

                    val searchText = query.trim()

                    val filteredProducts =
                        allProducts.filter { product ->

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
                                        (
                                                product.barcode?.contains(
                                                    searchText
                                                ) == true
                                                )

                            val matchesCategory =
                                category == null ||
                                        product.category == category

                            matchesQuery && matchesCategory

                        }.sortedBy {
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
                            selectedProduct =
                                current.selectedProduct,
                            errorMessage =
                                current.errorMessage
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
                        errorMessage =
                            "No se pudo cargar el catálogo."
                    )
                }
            }
        }
    }

    // =========================================
    // BUSCAR POR NOMBRE, SKU O CÓDIGO
    // =========================================

    fun onQueryChanged(query: String) {

        searchQuery.value = query

        _uiState.update {
            it.copy(
                errorMessage = null,
                selectedProduct = null
            )
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

        val normalizedBarcode = barcode.trim()

        if (normalizedBarcode.isBlank()) {

            _uiState.update {
                it.copy(
                    errorMessage =
                        "El código de barras está vacío."
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
                    searchQuery.value = normalizedBarcode

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
                                        "con ese código de barras."
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
                        errorMessage =
                            "No se pudo buscar el producto."
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
                        repository =
                            this.farmAppContainer()
                                .catalogProductRepository
                    )
                }
            }
    }
}