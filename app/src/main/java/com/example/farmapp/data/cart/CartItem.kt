package com.example.farmapp.data.cart

/**
 * HU04 - Modelo de un producto del carrito.
 *
 * Los precios se manejan en centimos para
 * evitar errores con numeros decimales.
 */
data class CartItem(
    val sku: String,
    val name: String,
    val imageUrl: String?,
    val unitPriceCents: Long,
    val quantity: Int
) {
    val subtotalCents: Long
        get() = unitPriceCents * quantity.toLong()
}