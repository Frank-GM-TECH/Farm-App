package com.example.farmapp.data.cart

data class CartItem(
    val sku: String,
    val name: String,
    val imageUrl: String?,
    val unitPriceCents: Long,
    val quantity: Int,

    // Indica que el precio difiere del guardado en Room.
    val priceChanged: Boolean = false,

    // Indica problemas de disponibilidad.
    val availabilityMessage: String? = null
) {
    val subtotalCents: Long
        get() = unitPriceCents * quantity.toLong()
}