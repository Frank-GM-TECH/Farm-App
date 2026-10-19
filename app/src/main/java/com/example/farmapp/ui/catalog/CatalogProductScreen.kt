package com.example.farmapp.ui.catalog

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.farmapp.catalog.CatalogProductUiState
import com.example.farmapp.catalog.CatalogProductViewModel
import com.example.farmapp.data.catalog.CatalogProductEntity
import java.util.Locale
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart

/**
 * HU03 - Ruta del catálogo persistente.
 */
@Composable
fun CatalogProductRoute(
    onBack: () -> Unit,
    catalogViewModel: CatalogProductViewModel =
        viewModel(factory = CatalogProductViewModel.Factory)
) {
    val uiState by catalogViewModel.uiState
        .collectAsStateWithLifecycle()

    CatalogProductScreen(
        uiState = uiState,
        onQueryChanged = catalogViewModel::onQueryChanged,
        onCategorySelected =
            catalogViewModel::onCategorySelected,
        onBack = onBack
    )
}

/**
 * Pantalla principal del catálogo de FarmApp.
 *
 * Muestra productos almacenados localmente en Room.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogProductScreen(
    uiState: CatalogProductUiState,
    onQueryChanged: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Catálogo FarmApp")
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Volver")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
                .testTag("hu03_catalog_screen"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "Explora nuestros productos",
                style = MaterialTheme.typography.titleLarge
            )

            // Búsqueda por nombre, SKU o código.
            OutlinedTextField(
                value = uiState.query,
                onValueChange = onQueryChanged,
                label = {
                    Text("Buscar productos")
                },
                placeholder = {
                    Text("Nombre, SKU o código de barras")
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hu03_search")
            )

            // Filtros horizontales de categoría.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                FilterChip(
                    selected = uiState.selectedCategory == null,
                    onClick = {
                        onCategorySelected(null)
                    },
                    label = {
                        Text("Todos")
                    }
                )

                uiState.categories.forEach { category ->

                    FilterChip(
                        selected =
                            uiState.selectedCategory == category,
                        onClick = {
                            onCategorySelected(category)
                        },
                        label = {
                            Text(categoryLabel(category))
                        }
                    )
                }
            }

            Text(
                text = "${uiState.products.size} productos encontrados",
                style = MaterialTheme.typography.bodyMedium
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.products.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No se encontraron productos.")
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(
                            minSize = 160.dp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("hu03_product_grid"),
                        contentPadding = PaddingValues(
                            bottom = 20.dp
                        ),
                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = uiState.products,
                            key = { it.sku }
                        ) { product ->

                            CatalogProductCard(
                                product = product
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta visual con fotografía, precio y stock.
 */
@Composable
fun CatalogProductCard(
    product: CatalogProductEntity,
    onAddToCart: ((CatalogProductEntity) -> Unit)? = null
) {
    val context = LocalContext.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hu03_product_${product.sku}")
    ) {

        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Fotografía del producto.

            AsyncImage(

                model = ImageRequest.Builder(context)
                    .data(product.imageUrl)
                    .setHeader(
                        "User-Agent",
                        "FarmApp/1.0 (https://github.com/Frank-GM-TECH/Farm-App)"
                    )
                    .crossfade(true)
                    .build(),

                contentDescription = "Imagen de ${product.name}",

                placeholder = painterResource(
                    android.R.drawable.ic_menu_gallery
                ),

                error = painterResource(
                    android.R.drawable.ic_menu_gallery
                ),

                fallback = painterResource(
                    android.R.drawable.ic_menu_gallery
                ),

                // Mostrar en Logcat el motivo real del error.
                onError = { state ->
                    Log.e(
                        "FarmAppImage",
                        "Error al cargar imagen del producto ${product.sku}",
                        state.result.throwable
                    )
                },

                contentScale = ContentScale.Fit,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .testTag("hu03_product_image")
            )
            Text(
                text = categoryLabel(product.category),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                minLines = 2,
                maxLines = 2
            )

            Text(
                text = formatPrice(product.priceCents),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = if (product.stock > 0) {
                    "Disponible: ${product.stock} unidades"
                } else {
                    "Agotado"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (product.stock > 0) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            // HU04 - Botón compartido del carrito.
            // Solo aparece cuando la pantalla
            // proporciona la acción de agregar.

            if (onAddToCart != null) {

                Button(
                    onClick = {
                        onAddToCart(product)
                    },
                    enabled = product.stock > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_to_cart_${product.sku}")
                ) {

                    Icon(
                        imageVector = Icons.Filled.AddShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = if (product.stock > 0) {
                            "Agregar"
                        } else {
                            "Agotado"
                        }
                    )
                }
            }

        }
    }
}

/**
 * Etiquetas legibles para los filtros.
 */
private fun categoryLabel(category: String): String {
    return when (category) {
        "HIGIENE" -> "Higiene"
        "PRIMEROS_AUXILIOS" -> "Primeros auxilios"
        "EQUIPOS" -> "Equipos"
        else -> category
    }
}

/**
 * Precio en soles para los datos de demostración.
 */
private fun formatPrice(priceCents: Long): String {
    return String.format(
        Locale.US,
        "S/ %.2f",
        priceCents / 100.0
    )
}