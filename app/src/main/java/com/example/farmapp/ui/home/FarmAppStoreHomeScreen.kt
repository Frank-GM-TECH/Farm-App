package com.example.farmapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.farmapp.catalog.CatalogProductUiState
import com.example.farmapp.ui.catalog.CatalogProductCard
import com.example.farmapp.data.catalog.CatalogProductEntity

/**
 * HU03 - Pantalla de inicio comercial de FarmApp.
 *
 * Incluye:
 * - Dirección de entrega
 * - Acceso a Delivery
 * - Acceso al carrito
 * - Búsqueda de productos
 * - Banner promocional de demostración
 * - Productos disponibles desde Room
 *
 * La navegación se conectará en el siguiente paso.
 */

@Composable
fun FarmAppStoreHomeScreen(
    uiState: CatalogProductUiState,
    onQueryChanged: (String) -> Unit,
    onOpenAddress: () -> Unit,
    onOpenDelivery: () -> Unit,
    onOpenCart: () -> Unit,
    modifier: Modifier = Modifier,
    onAddToCart: ((CatalogProductEntity) -> Unit)? = null,
    cartItemCount: Int = 0
) {

    val availableProducts = uiState.products.filter {
        it.stock > 0
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .testTag("farmapp_store_home")
    ) {

        // =====================================
        // ENCABEZADO
        // =====================================

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            TextButton(onClick = onOpenAddress) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null
                )
                Spacer(Modifier.width(4.dp))
                Text("Elegir dirección")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                TextButton(onClick = onOpenDelivery) {
                    Text("Delivery")
                }


                IconButton(
                    onClick = onOpenCart,
                    modifier = Modifier.testTag("home_open_cart")
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

        // =====================================
        // BUSCADOR
        // =====================================

        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChanged,
            label = {
                Text("¿Qué producto buscas?")
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home_product_search")
        )

        Spacer(Modifier.height(12.dp))

        // =====================================
        // CONTENIDO DESPLAZABLE
        // =====================================

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Banner promocional de muestra.
            item(span = { GridItemSpan(maxLineSpan) }) {
                FarmAppPromotionBanner()
            }

            // Encabezado de productos.
            item(span = { GridItemSpan(maxLineSpan) }) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    Text(
                        text = "Productos disponibles",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "${availableProducts.size} productos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Estado de carga.
            if (uiState.isLoading) {

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

            } else {

                // Error al cargar el catálogo.
                if (uiState.errorMessage != null) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Catálogo vacío.
                if (availableProducts.isEmpty()) {

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "No hay productos disponibles.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                } else {

                    // Productos reales de Room.
                    items(
                        items = availableProducts,
                        key = { it.sku }
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

/**
 * Banner estático.
 *
 * No es clicable ni ejecuta promociones.
 * Más adelante se podrá reemplazar por
 * una imagen drawable propia de FarmApp.
 */
@Composable
private fun FarmAppPromotionBanner() {

    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF087B4A),
            Color(0xFF1BB778)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = gradient,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
            .testTag("home_promotion_banner"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocalOffer,
                contentDescription = null,
                tint = Color.White
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = "PROMOCIÓN DE DEMOSTRACIÓN",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
        }

        Text(
            text = "¡Ofertas por tiempo limitado!",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Descubre productos para tu salud y bienestar.",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "Imagen promocional ilustrativa",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}