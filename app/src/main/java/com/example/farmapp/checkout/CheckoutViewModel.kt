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

    // =====================================
    // REINICIAR BORRADOR
    // =====================================

    fun resetCheckout() {
        _uiState.value = OrderDetailUiState()
    }
}