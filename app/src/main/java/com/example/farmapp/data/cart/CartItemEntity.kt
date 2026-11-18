package com.example.farmapp.data.cart

import androidx.room.Entity
import androidx.room.Index

/**
 * HU04 - Tabla de productos agregados al carrito.
 *
 * La clave compuesta impide duplicar el mismo
 * producto dentro del carrito de un usuario.
 *
 * userId debe corresponder al UID autenticado
 * en Firebase Authentication.
 */
@Entity(
    tableName = "cart_items",
    primaryKeys = ["userId", "sku"],
    indices = [Index(value = ["userId"])]
)
data class CartItemEntity(
    val userId: String,
    val sku: String,
    val name: String,
    val imageUrl: String?,
    val unitPriceCents: Long,
    val quantity: Int
) {
    fun toCartItem(): CartItem {
        return CartItem(
            sku = sku,
            name = name,
            imageUrl = imageUrl,
            unitPriceCents = unitPriceCents,
            quantity = quantity
        )
    }
}