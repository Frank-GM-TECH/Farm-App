package com.example.farmapp.catalog

/**
 * Estado completo e INMUTABLE de la UI didáctica del catálogo.
 * La interfaz muestra esta instantánea, sin calcular filtros dentro de Compose.
 */
data class CatalogUiState(
    val query: String,
    val selectedCategory: DemoCategory?,
    val snapshot: CatalogSnapshot<DemoProduct>
)
