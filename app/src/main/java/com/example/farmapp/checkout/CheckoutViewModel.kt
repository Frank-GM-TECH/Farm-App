package com.example.farmapp.checkout

import androidx.lifecycle.ViewModel
import com.example.farmapp.ui.checkout.OrderDeliveryMethod
import com.example.farmapp.ui.checkout.OrderDetailUiState
import com.example.farmapp.ui.checkout.PickupRecipient
import com.example.farmapp.ui.checkout.PickupStoreUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import com.example.farmapp.cart.CartUiState
import com.example.farmapp.data.orders.CreateOrderItemInput
import com.example.farmapp.data.orders.CreateOrderRequest
import com.example.farmapp.data.orders.FirebaseCreateOrderRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.UUID


/**
 * HU04 - Gestiona el borrador del pedido.
 *
 * Conserva el estado mientras el usuario
 * navega por las pantallas del checkout.
 *
 * No procesa pagos reales.
 */
class CheckoutViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow(OrderDetailUiState())

    val uiState: StateFlow<OrderDetailUiState> =
        _uiState.asStateFlow()
    private val createOrderRepository by lazy {
        FirebaseCreateOrderRepository()
    }

    private var pendingRequest: CreateOrderRequest? = null

    // =====================================
    // TIPO DE ENTREGA
    // =====================================

    fun selectDeliveryMethod(
        method: OrderDeliveryMethod
    ) {
        _uiState.update { current ->

            when (method) {

                OrderDeliveryMethod.DELIVERY -> {
                    current.copy(
                        deliveryMethod = method,
                        pickupStore = null,
                        pickupRecipient = PickupRecipient.SELF,
                        otherRecipientName = ""
                    )
                }

                OrderDeliveryMethod.PICKUP -> {
                    current.copy(
                        deliveryMethod = method,
                        deliveryAddress = null
                    )
                }
            }
        }
    }

    // =====================================
    // DIRECCIÓN DE ENTREGA
    // =====================================

    fun setDeliveryAddress(address: String) {
        _uiState.update {
            it.copy(
                deliveryMethod = OrderDeliveryMethod.DELIVERY,
                deliveryAddress =
                    address.trim().takeIf(String::isNotBlank),
                pickupStore = null
            )
        }
    }

    // =====================================
    // BOTICA SELECCIONADA
    // =====================================

    fun setPickupStore(store: PickupStoreUi) {
        _uiState.update {
            it.copy(
                deliveryMethod = OrderDeliveryMethod.PICKUP,
                deliveryAddress = null,
                pickupStore = store
            )
        }
    }

    // =====================================
    // PERSONA QUE RECOGE
    // =====================================

    fun selectPickupRecipient(
        recipient: PickupRecipient
    ) {
        _uiState.update {
            it.copy(
                pickupRecipient = recipient,
                otherRecipientName =
                    if (recipient == PickupRecipient.SELF)
                        ""
                    else
                        it.otherRecipientName
            )
        }
    }

    fun setOtherRecipientName(name: String) {
        _uiState.update {
            it.copy(
                otherRecipientName = name
            )
        }
    }

    // =====================================
    // MÉTODO DE PAGO
    // =====================================

    fun setPaymentMethod(method: String) {
        _uiState.update {
            it.copy(
                paymentMethod =
                    method.trim().takeIf(String::isNotBlank)
            )
        }
    }

    // =====================================
    // COMPROBANTE DE PAGO
    // =====================================

    fun selectBoleta() {
        _uiState.update {
            it.copy(
                receiptType = "Boleta simple",
                invoiceRuc = "",
                invoiceBusinessName = "",
                invoiceValid = false
            )
        }
    }

    fun selectFactura() {
        _uiState.update {
            it.copy(
                receiptType = "Factura",
                invoiceValid = false
            )
        }
    }

    fun setInvoiceValid(valid: Boolean) {
        _uiState.update {
            it.copy(invoiceValid = valid)
        }
    }

    fun setInvoiceDetails(
        ruc: String,
        businessName: String
    ) {
        val cleanRuc = ruc.trim()
        val cleanName = businessName.trim()

        val valid =
            cleanRuc.length == 11 &&
                    cleanRuc.all { it.isDigit() } &&
                    cleanName.length >= 3

        _uiState.update {
            it.copy(
                receiptType = "Factura",
                invoiceRuc = cleanRuc,
                invoiceBusinessName = cleanName,
                invoiceValid = valid
            )
        }
    }

    // =====================================
    // CUPÓN DE DESCUENTO
    // =====================================


    fun setCoupon(
        code: String,
        discountCents: Long
    ) {
        require(discountCents >= 0L) {
            "El descuento no puede ser negativo."
        }

        val normalizedCode = code.trim().uppercase()

        _uiState.update {
            it.copy(
                couponCode = normalizedCode.takeIf {
                        value -> value.isNotBlank()
                },
                discountCents = discountCents
            )
        }
    }

    /**
     * Recalcula el descuento cuando cambia
     * el subtotal del carrito.
     */
    fun updateCartSubtotal(subtotalCents: Long) {
        _uiState.update { current ->

            val updatedDiscount =
                CouponPricing.calculateDiscount(
                    couponCode = current.couponCode,
                    subtotalCents = subtotalCents
                )

            if (current.discountCents == updatedDiscount) {
                current
            } else {
                current.copy(
                    discountCents = updatedDiscount
                )
            }
        }
    }


    fun removeCoupon() {
        _uiState.update {
            it.copy(
                couponCode = null,
                discountCents = 0L
            )
        }
    }

    fun confirmOrder(cart: CartUiState) {
        val state = _uiState.value

        if (state.isProcessing || state.confirmedOrderId != null) {
            return
        }

        fun showError(message: String) {
            _uiState.update {
                it.copy(submissionError = message)
            }
        }

        if (!cart.canCheckout) {
            showError("Revisa la disponibilidad de los productos.")
            return
        }

        val deliveryMethod = state.deliveryMethod
        if (deliveryMethod == null) {
            showError("Selecciona un método de entrega.")
            return
        }

        // Adaptación temporal de los métodos de pago de demostración.
        val paymentMethod = when (state.paymentMethod) {
            "Yape / Plin" -> "YAPE"
            "Tarjeta de débito o crédito" -> "TARJETA_DEMO"
            "Pago contraentrega" -> "EFECTIVO"
            else -> {
                showError("Selecciona un método de pago válido.")
                return
            }
        }

        val receiptType = state.receiptType
        if (receiptType == null) {
            showError("Selecciona un comprobante.")
            return
        }

        if (receiptType == "Factura" && !state.invoiceValid) {
            showError("Completa los datos de facturación.")
            return
        }

        val deliveryAddress =
            if (deliveryMethod.name == "DELIVERY") {
                state.deliveryAddress
            } else {
                null
            }

        val pickupStoreId =
            if (deliveryMethod.name == "PICKUP") {
                state.pickupStore?.id
            } else {
                null
            }

        if (
            deliveryMethod.name == "DELIVERY" &&
            deliveryAddress.isNullOrBlank()
        ) {
            showError("Selecciona una dirección de entrega.")
            return
        }

        if (
            deliveryMethod.name == "PICKUP" &&
            pickupStoreId.isNullOrBlank()
        ) {
            showError("Selecciona una botica.")
            return
        }

        val proposedRequest = CreateOrderRequest(
            requestId = "APP-" +
                    UUID.randomUUID().toString().replace("-", ""),
            items = cart.items.map { item ->
                CreateOrderItemInput(
                    sku = item.sku,
                    quantity = item.quantity
                )
            }.sortedBy { it.sku },
            deliveryMethod = deliveryMethod.name,
            paymentMethod = paymentMethod,
            receiptType = receiptType,
            couponCode = state.couponCode,
            deliveryAddress = deliveryAddress,
            pickupStoreId = pickupStoreId,
            invoiceRuc = if (receiptType == "Factura") {
                state.invoiceRuc
            } else {
                null
            },
            invoiceBusinessName = if (receiptType == "Factura") {
                state.invoiceBusinessName
            } else {
                null
            }
        )

        // Conservamos el mismo requestId cuando reintentamos.
        // Si el pedido cambió, primero habrá que resolver
        // el intento pendiente para evitar duplicaciones.
        val previousRequest = pendingRequest

        if (
            previousRequest != null &&
            previousRequest.copy(
                requestId = proposedRequest.requestId
            ) != proposedRequest
        ) {
            showError(
                "Hay un intento pendiente con otros datos. " +
                        "No generaremos un segundo pedido automáticamente."
            )
            return
        }

        val request = previousRequest ?: proposedRequest.also {
            pendingRequest = it
        }

        _uiState.update {
            it.copy(
                isProcessing = true,
                submissionError = null
            )
        }

        viewModelScope.launch {
            try {
                val result = createOrderRepository.createOrder(request)

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        confirmedOrderId = result.orderId,
                        confirmedTotalCents = result.totalCents,
                        submissionError = null
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {

                android.util.Log.e(
                    "FarmAppCheckout",
                    "Error al confirmar pedido en Cloud Functions",
                    error
                )

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        submissionError =
                            "No se pudo confirmar el pedido. " +
                                    "Revisa la conexión y reintenta " +
                                    "sin cambiar sus datos."
                    )
                }
            }
        }
    }

    // =====================================
    // REINICIAR BORRADOR
    // =====================================

    fun resetCheckout() {
        pendingRequest = null
        _uiState.value = OrderDetailUiState()
    }
}