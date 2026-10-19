package com.example.farmapp.ui.checkout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.farmapp.cart.CartUiState
import java.util.Locale

/**
 * HU04 - Formas de recibir un pedido.
 */
enum class OrderDeliveryMethod {
    DELIVERY,
    PICKUP
}

/**
 * HU04 - Persona que recogerá el pedido.
 */
enum class PickupRecipient {
    SELF,
    OTHER
}

/**
 * Datos de una botica seleccionada.
 *
 * No se asignan distancias ni horarios
 * inventados. Los datos vendrán del
 * catálogo de establecimientos.
 */
data class PickupStoreUi(
    val id: String,
    val name: String,
    val address: String,
    val distanceKm: Double? = null,
    val openingHours: String? = null,
    val estimatedReady: String? = null
)

/**
 * HU04 - Borrador del detalle del pedido.
 *
 * Posteriormente será administrado por
 * CheckoutViewModel y sus repositorios.
 */
data class OrderDetailUiState(
    val deliveryMethod: OrderDeliveryMethod? = null,
    val deliveryAddress: String? = null,
    val pickupStore: PickupStoreUi? = null,
    val pickupRecipient: PickupRecipient = PickupRecipient.SELF,
    val otherRecipientName: String = "",
    val paymentMethod: String? = null,
    val receiptType: String? = null,
    val invoiceValid: Boolean = false,
    val couponCode: String? = null,
    val discountCents: Long = 0L,
    val isProcessing: Boolean = false
)

/**
 * HU04 - Pantalla Detalle de tu pedido.
 *
 * No realiza pagos ni emite comprobantes.
 * Todas las acciones se reciben mediante
 * callbacks para conectarlas después.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmAppOrderDetailScreen(
    cartUiState: CartUiState,
    orderUiState: OrderDetailUiState,
    onBack: () -> Unit,
    onDeliveryMethodSelected: (OrderDeliveryMethod) -> Unit,
    onOpenAddress: () -> Unit,
    onChoosePickupStore: () -> Unit,
    onPickupRecipientSelected: (PickupRecipient) -> Unit,
    onOtherRecipientNameChanged: (String) -> Unit,
    onOpenPaymentMethod: () -> Unit,
    onOpenReceipt: () -> Unit,
    onOpenCoupon: () -> Unit,
    onConfirmOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showProducts by rememberSaveable {
        mutableStateOf(false)
    }

    val subtotal = cartUiState.subtotalCents

    val validDiscount =
        orderUiState.discountCents >= 0L &&
                orderUiState.discountCents <= subtotal

    val discount = if (validDiscount) {
        orderUiState.discountCents
    } else {
        0L
    }

    val total = subtotal - discount

    val hasDeliverySelection = when (
        orderUiState.deliveryMethod
    ) {
        OrderDeliveryMethod.DELIVERY ->
            !orderUiState.deliveryAddress.isNullOrBlank()

        OrderDeliveryMethod.PICKUP ->
            orderUiState.pickupStore != null &&
                    (
                            orderUiState.pickupRecipient ==
                                    PickupRecipient.SELF ||
                                    orderUiState.otherRecipientName.isNotBlank()
                            )

        null -> false
    }

    val hasReceipt =
        !orderUiState.receiptType.isNullOrBlank() &&
                (
                        orderUiState.receiptType != "Factura" ||
                                orderUiState.invoiceValid
                        )

    val canConfirm =
        cartUiState.items.isNotEmpty() &&
                !cartUiState.isLoading &&
                !cartUiState.isWorking &&
                !orderUiState.isProcessing &&
                hasDeliverySelection &&
                !orderUiState.paymentMethod.isNullOrBlank() &&
                hasReceipt &&
                validDiscount

    Scaffold(
        modifier = modifier.testTag("order_detail_screen"),

        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Detalle de tu pedido")
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
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    AmountRow(
                        label = "Subtotal",
                        amount = subtotal
                    )

                    if (discount > 0) {
                        AmountRow(
                            label = "Descuento",
                            amount = -discount
                        )
                    }

                    HorizontalDivider()

                    AmountRow(
                        label = "Total",
                        amount = total,
                        emphasized = true
                    )

                    Button(
                        onClick = onConfirmOrder,
                        enabled = canConfirm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("order_confirm_button")
                    ) {
                        Text("Confirmar pedido")
                    }
                }
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("order_detail_content"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {

            // =================================
            // RECEPCIÓN DEL PEDIDO
            // =================================

            item {
                OrderSection {
                    Text(
                        text = "¿Cómo quieres recibir tu pedido?",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            onDeliveryMethodSelected(
                                OrderDeliveryMethod.DELIVERY
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (
                                orderUiState.deliveryMethod ==
                                OrderDeliveryMethod.DELIVERY
                            ) {
                                "✓ Entrega a una dirección"
                            } else {
                                "Entrega a una dirección"
                            }
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            onDeliveryMethodSelected(
                                OrderDeliveryMethod.PICKUP
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (
                                orderUiState.deliveryMethod ==
                                OrderDeliveryMethod.PICKUP
                            ) {
                                "✓ Recojo en botica"
                            } else {
                                "Recojo en botica"
                            }
                        )
                    }

                    // ENTREGA A DIRECCIÓN
                    if (
                        orderUiState.deliveryMethod ==
                        OrderDeliveryMethod.DELIVERY
                    ) {
                        HorizontalDivider()

                        Text(
                            text = orderUiState.deliveryAddress
                                ?: "Todavía no has seleccionado una dirección."
                        )

                        TextButton(
                            onClick = onOpenAddress
                        ) {
                            Text("Agregar o cambiar dirección")
                        }

                        Text(
                            text = "El costo y plazo de delivery " +
                                    "se calcularán más adelante.",
                            style = MaterialTheme.typography.bodySmall,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // RECOJO EN BOTICA
                    if (
                        orderUiState.deliveryMethod ==
                        OrderDeliveryMethod.PICKUP
                    ) {
                        HorizontalDivider()

                        Text(
                            text = "Lugar de recojo",
                            style = MaterialTheme.typography.titleMedium
                        )

                        val store = orderUiState.pickupStore

                        if (store != null) {
                            Text(
                                text = store.name,
                                style =
                                    MaterialTheme.typography.titleSmall
                            )

                            Text(store.address)

                            store.distanceKm?.let {
                                Text(
                                    String.format(
                                        Locale.US,
                                        "Distancia estimada: %.1f km",
                                        it
                                    )
                                )
                            }

                            Text(
                                text = store.openingHours
                                    ?: "Horario por confirmar"
                            )
                        } else {
                            Text(
                                "Selecciona una botica para recoger tu pedido."
                            )
                        }

                        TextButton(
                            onClick = onChoosePickupStore
                        ) {
                            Text("Seleccionar o cambiar botica")
                        }

                        HorizontalDivider()

                        Text(
                            text = "¿Quién recogerá tu pedido?",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected =
                                    orderUiState.pickupRecipient ==
                                            PickupRecipient.SELF,
                                onClick = {
                                    onPickupRecipientSelected(
                                        PickupRecipient.SELF
                                    )
                                }
                            )
                            Text("Yo")

                            Spacer(Modifier.width(20.dp))

                            RadioButton(
                                selected =
                                    orderUiState.pickupRecipient ==
                                            PickupRecipient.OTHER,
                                onClick = {
                                    onPickupRecipientSelected(
                                        PickupRecipient.OTHER
                                    )
                                }
                            )
                            Text("Otra persona")
                        }

                        if (
                            orderUiState.pickupRecipient ==
                            PickupRecipient.OTHER
                        ) {
                            OutlinedTextField(
                                value =
                                    orderUiState.otherRecipientName,
                                onValueChange =
                                    onOtherRecipientNameChanged,
                                label = {
                                    Text("Nombre de quien recogerá")
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        HorizontalDivider()

                        Text(
                            text = "Horario de recojo",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = store?.estimatedReady
                                ?: "Pendiente de confirmación"
                        )

                        Text(
                            text = "Política de demostración: " +
                                    "hasta 3 días hábiles desde que " +
                                    "el pedido esté listo.",
                            style = MaterialTheme.typography.bodySmall,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // =================================
            // MÉTODO DE PAGO
            // =================================

            item {
                OrderSection {
                    SectionAction(
                        title = "Método de pago",
                        value = orderUiState.paymentMethod
                            ?: "Sin seleccionar",
                        onClick = onOpenPaymentMethod
                    )
                }
            }

            // =================================
            // COMPROBANTE
            // =================================

            item {
                OrderSection {
                    SectionAction(
                        title = "Comprobante de pago",
                        value = orderUiState.receiptType
                            ?: "Sin seleccionar",
                        onClick = onOpenReceipt
                    )
                }
            }

            // =================================
            // CUPÓN OPCIONAL
            // =================================

            item {
                OrderSection {
                    SectionAction(
                        title = "Cupón de descuento",
                        value = orderUiState.couponCode
                            ?: "Opcional",
                        onClick = onOpenCoupon
                    )
                }
            }

            // =================================
            // MI PEDIDO EXPANDIBLE
            // =================================

            item {
                OrderSection {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment =
                            Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Mi pedido (${cartUiState.totalUnits})",
                            style =
                                MaterialTheme.typography.titleMedium
                        )

                        IconButton(
                            onClick = {
                                showProducts = !showProducts
                            },
                            modifier = Modifier.testTag(
                                "order_toggle_products"
                            )
                        ) {
                            Icon(
                                imageVector =
                                    if (showProducts)
                                        Icons.Filled.ExpandLess
                                    else
                                        Icons.Filled.ExpandMore,
                                contentDescription =
                                    "Mostrar u ocultar productos"
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = showProducts
                    ) {
                        Column(
                            verticalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {
                            cartUiState.items.forEach { product ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text =
                                            "${product.name} x${product.quantity}",
                                        modifier = Modifier.weight(1f),
                                        style =
                                            MaterialTheme.typography.bodyMedium
                                    )

                                    Spacer(Modifier.width(8.dp))

                                    Text(
                                        formatSoles(
                                            product.subtotalCents
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Contenedor visual de cada sección.
 */
@Composable
private fun OrderSection(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

/**
 * Fila que abre otra pantalla.
 */
@Composable
private fun SectionAction(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement =
                Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TextButton(onClick = onClick) {
            Text("Cambiar")
        }
    }
}

/**
 * Fila de precio.
 */
@Composable
private fun AmountRow(
    label: String,
    amount: Long,
    emphasized: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (emphasized)
                MaterialTheme.typography.titleLarge
            else
                MaterialTheme.typography.bodyMedium
        )

        Text(
            text = formatSoles(amount),
            style = if (emphasized)
                MaterialTheme.typography.titleLarge
            else
                MaterialTheme.typography.bodyMedium,
            color = if (emphasized)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatSoles(cents: Long): String {
    return String.format(
        Locale.US,
        "S/ %.2f",
        cents / 100.0
    )
}