package com.example.farmapp.ui.cart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.farmapp.cart.CartUiState
import com.example.farmapp.data.cart.CartItem
import java.text.NumberFormat
import java.util.Locale
import com.example.farmapp.ui.catalog.farmAppProductImage

/**
 * HU04 - Pantalla principal del carrito de FarmApp.
 *
 * No depende directamente de NavController ni de Room.
 * Recibe el estado del CartViewModel y callbacks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmAppCartScreen(
    uiState: CartUiState,
    onBack: () -> Unit,
    onContinueShopping: () -> Unit,
    onOpenFavorites: () -> Unit,
    onIncreaseQuantity: (CartItem) -> Unit,
    onDecreaseQuantity: (CartItem) -> Unit,
    onRemoveProduct: (CartItem) -> Unit,
    onCheckout: () -> Unit,
    onDismissMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasProducts = uiState.items.isNotEmpty()

    Scaffold(
        modifier = modifier.testTag("farmapp_cart_screen"),

        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Mi carrito")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },

        bottomBar = {
            if (hasProducts && !uiState.isLoading) {
                CartSummaryPanel(
                    uiState = uiState,
                    onCheckout = onCheckout
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ====================================
            // PESTAÑAS DEL CARRITO
            // ====================================

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                FilledTonalButton(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(Modifier.width(6.dp))

                    Text("Mi carrito")
                }

                OutlinedButton(
                    onClick = onOpenFavorites,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("cart_open_favorites")
                ) {
                    Icon(
                        imageVector = Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(Modifier.width(6.dp))

                    Text("Mis favoritos")
                }
            }

            Spacer(Modifier.height(12.dp))

            // ====================================
            // MENSAJES DEL VIEWMODEL
            // ====================================

            uiState.message?.let { message ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {

                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = message,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )

                        TextButton(
                            onClick = onDismissMessage
                        ) {
                            Text("Cerrar")
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
            }

            // ====================================
            // ESTADOS DEL CARRITO
            // ====================================

            when {

                uiState.isLoading -> {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                !hasProducts -> {

                    EmptyCartContent(
                        onContinueShopping = onContinueShopping,
                        modifier = Modifier.weight(1f)
                    )
                }

                else -> {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("cart_products_list"),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        item {
                            Text(
                                text = "FarmApp (${uiState.totalUnits})",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(
                                    vertical = 8.dp
                                )
                            )
                        }

                        items(
                            items = uiState.items,
                            key = { it.sku }
                        ) { item ->

                            CartProductCard(
                                item = item,
                                enabled = !uiState.isWorking,
                                onIncrease = {
                                    onIncreaseQuantity(item)
                                },
                                onDecrease = {
                                    onDecreaseQuantity(item)
                                },
                                onRemove = {
                                    onRemoveProduct(item)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Estado inicial del carrito vacío.
 */
@Composable
private fun EmptyCartContent(
    onContinueShopping: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .testTag("cart_empty_state"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            imageVector = Icons.Filled.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(86.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Tu carrito está vacío",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Una vez que añadas productos, " +
                    "aparecerán aquí.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onContinueShopping,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Ir a comprar")
        }
    }
}

/**
 * Tarjeta individual de producto.
 */
@Composable
private fun CartProductCard(
    item: CartItem,
    enabled: Boolean,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {

    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cart_item_${item.sku}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Foto del producto con el User-Agent
                // que solucionó el error 403 en HU03.
                if (
                    farmAppProductImage(item.sku) != null ||
                    !item.imageUrl.isNullOrBlank()
                ) {

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(
                                farmAppProductImage(item.sku)
                                    ?: item.imageUrl
                            )
                            .setHeader(
                                "User-Agent",
                                "FarmApp/1.0 " +
                                        "(https://github.com/" +
                                        "Frank-GM-TECH/Farm-App)"
                            )
                            .crossfade(true)
                            .build(),

                        contentDescription = item.name,

                        contentScale = ContentScale.Fit,

                        error = painterResource(
                            android.R.drawable.ic_menu_gallery
                        ),

                        modifier = Modifier
                            .size(96.dp)
                    )

                } else {

                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(96.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "SKU: ${item.sku}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = formatSoles(item.unitPriceCents),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (item.priceChanged) {
                        Text(
                            text = "Precio actualizado según el catálogo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    item.availabilityMessage?.let { warning ->
                        Text(
                            text = warning,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                TextButton(
                    onClick = onRemove,
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(Modifier.width(4.dp))
                    Text("Eliminar")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = onDecrease,
                        enabled = enabled
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Disminuir cantidad"
                        )
                    }

                    Text(
                        text = item.quantity.toString(),
                        style = MaterialTheme.typography.titleMedium
                    )

                    IconButton(
                        onClick = onIncrease,
                        enabled = enabled &&
                                item.quantity < Int.MAX_VALUE
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Aumentar cantidad"
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Subtotal: " +
                            formatSoles(item.subtotalCents),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

/**
 * Resumen de pedido expandible.
 * Se mantiene visible en la parte inferior.
 */
@Composable
private fun CartSummaryPanel(
    uiState: CartUiState,
    onCheckout: () -> Unit
) {

    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

    Surface(
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp
        ),
        color = MaterialTheme.colorScheme.surface
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            TextButton(
                onClick = {
                    expanded = !expanded
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cart_toggle_summary")
            ) {

                Text(
                    text = "Resumen de pedido",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium
                )

                Icon(
                    imageVector =
                        if (expanded) Icons.Filled.ExpandMore
                        else Icons.Filled.ExpandLess,
                    contentDescription =
                        if (expanded) "Contraer resumen"
                        else "Desplegar resumen"
                )
            }

            AnimatedVisibility(visible = expanded) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    uiState.items.forEach { item ->

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {

                            Text(
                                text = "${item.name} x${item.quantity}",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(
                                text = formatSoles(
                                    item.subtotalCents
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        Text("Subtotal")

                        Text(
                            formatSoles(uiState.subtotalCents)
                        )
                    }
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = formatSoles(uiState.subtotalCents),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }


            if (uiState.unavailableItems.isNotEmpty()) {
                Text(
                    text = "No puedes continuar: " +
                            "hay productos agotados, desactivados " +
                            "o con stock insuficiente. " +
                            "Revisa las cantidades o elimínalos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag(
                        "cart_checkout_blocked_warning"
                    )
                )
            }

            Button(
                onClick = onCheckout,
                enabled = uiState.canCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("cart_go_checkout")
            ) {
                Text("Ir a comprar")
            }
        }
    }
}

/**
 * Dar formato peruano a los precios.
 * Los importes se mantienen como Long
 * en centimos hasta el momento de mostrarlos.
 */
private fun formatSoles(cents: Long): String {

    val formatter = NumberFormat.getCurrencyInstance(
        Locale.forLanguageTag("es-PE")
    )

    return formatter.format(cents / 100.0)
}