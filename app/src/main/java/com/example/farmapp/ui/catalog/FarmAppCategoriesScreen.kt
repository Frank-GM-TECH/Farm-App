package com.example.farmapp.ui.catalog

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.farmapp.catalog.CatalogProductUiState
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import com.example.farmapp.data.catalog.CatalogProductEntity

/**
 * HU03 - Pantalla comercial de Categorías.
 *
 * Esta pantalla es independiente del NavController.
 * Recibe el estado del catálogo Room a través del ViewModel.
 *
 * Incluye:
 * - Búsqueda por nombre o código
 * - Filtros por categoría
 * - Productos con imagen, precio y stock
 * - Estados de carga, error y lista vacía
 */

@Composable
fun FarmAppCategoriesScreen(
    uiState: CatalogProductUiState,
    onQueryChanged: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onOpenScanner: () -> Unit = {},
    modifier: Modifier = Modifier,
    onAddToCart: ((CatalogProductEntity) -> Unit)? = null,
    onOpenCart: (() -> Unit)? = null,
    cartItemCount: Int = 0
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("farmapp_categories_screen")
    ) {

        // ====================================
        // TÍTULO
        // ====================================

        Spacer(modifier = Modifier.height(16.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Categorías",
                style = MaterialTheme.typography.headlineMedium
            )


            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Escáner HU03: se conserva.
                IconButton(
                    onClick = onOpenScanner,
                    modifier = Modifier.testTag("categories_open_scanner")
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = "Escanear código de barras",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Carrito HU04.
                if (onOpenCart != null) {

                    IconButton(
                        onClick = onOpenCart,
                        modifier = Modifier.testTag("categories_open_cart")
                    ) {
                        BadgedBox(
                            badge = {
                                if (cartItemCount > 0) {
                                    Badge {
                                        Text(cartItemCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ShoppingCart,
                                contentDescription = "Mi carrito"
                            )
                        }
                    }
                }
            }

        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Encuentra productos para tu salud y bienestar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ====================================
        // BUSCADOR
        // ====================================

        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("categories_product_search"),
            placeholder = {
                Text("Buscar productos...")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null
                )
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ====================================
        // FILTROS
        // ====================================

        Text(
            text = "Explorar categorías",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .testTag("categories_filters"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            FilterChip(
                selected = uiState.selectedCategory == null,
                onClick = {
                    onCategorySelected(null)
                },
                label = {
                    Text("Todas")
                },
                modifier = Modifier.testTag(
                    "categories_filter_all"
                )
            )

            uiState.categories.forEach { category ->

                FilterChip(
                    selected = uiState.selectedCategory == category,
                    onClick = {
                        onCategorySelected(category)
                    },
                    label = {
                        Text(category)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ====================================
        // RESULTADOS
        // ====================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Productos",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "${uiState.products.size} resultados",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ====================================
        // GRID DE PRODUCTOS ROOM
        // ====================================

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("categories_products_grid"),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                bottom = 20.dp
            )
        ) {

            if (uiState.isLoading) {

                item(
                    span = {
                        GridItemSpan(maxLineSpan)
                    }
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        CircularProgressIndicator()

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text("Cargando productos...")
                    }
                }

            } else {

                // Mensaje de error.
                if (uiState.errorMessage != null) {

                    item(
                        span = {
                            GridItemSpan(maxLineSpan)
                        }
                    ) {

                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Sin resultados.
                if (uiState.products.isEmpty()) {

                    item(
                        span = {
                            GridItemSpan(maxLineSpan)
                        }
                    ) {

                        Text(
                            text = "No encontramos productos para esta búsqueda.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(20.dp)
                        )
                    }

                } else {

                    // Tarjetas reutilizadas de HU03.
                    items(
                        items = uiState.products,
                        key = { product -> product.sku }
                    ) { product ->

                        CatalogProductCard(
                            product = product,
                            onAddToCart = onAddToCart
                        )
                    }
                }
            }
        }
    }
}