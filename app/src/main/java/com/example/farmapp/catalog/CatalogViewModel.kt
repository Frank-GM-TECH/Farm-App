package com.example.farmapp.catalog

import androidx.lifecycle.SavedStateHandle
import com.example.farmapp.data.farmAppContainer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Cotejo 14: ViewModel del catálogo educativo (preparación de HU03).
 *
 * - Retiene uiState durante rotaciones mientras sobreviva este ViewModel.
 * - SavedStateHandle conserva solo los filtros pequeños, no el repositorio.
 * - Los resultados se recalculan desde CatalogAnalyzer.
 * - No almacena datos de usuarios, contraseñas, stock real ni pedidos.
 */
class CatalogViewModel(
    private val savedStateHandle: SavedStateHandle,
    repository: CatalogRepository<DemoProduct>
) : ViewModel() {

    private val analyzer = CatalogAnalyzer(repository)

    private val _uiState = MutableStateFlow(
        buildState(
            query = savedStateHandle[QUERY_KEY] ?: "",
            category = (savedStateHandle.get<String>(CATEGORY_KEY) ?: "")
                .let { saved -> DemoCategory.entries.firstOrNull { it.name == saved } }
        )
    )
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    /** Evento emitido por OutlinedTextField (UI -> ViewModel). */
    fun onQueryChanged(newQuery: String) {
        setFilters(newQuery, _uiState.value.selectedCategory)
    }

    /** Evento emitido por FilterChip (UI -> ViewModel). */
    fun onCategorySelected(category: DemoCategory?) {
        setFilters(_uiState.value.query, category)
    }

    private fun setFilters(query: String, category: DemoCategory?) {
        if (_uiState.value.query == query && _uiState.value.selectedCategory == category) return
        savedStateHandle[QUERY_KEY] = query
        savedStateHandle[CATEGORY_KEY] = category?.name ?: ""
        _uiState.value = buildState(query, category)
    }

    private fun buildState(query: String, category: DemoCategory?): CatalogUiState =
        CatalogUiState(query, category, analyzer.search(query, category))

    companion object {
        const val QUERY_KEY = "catalog_query"
        const val CATEGORY_KEY = "catalog_category"

        /** Misma forma de inicialización que AuthViewModel.Factory. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CatalogViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = this.farmAppContainer().catalogRepository
                )
            }
        }
    }
}
