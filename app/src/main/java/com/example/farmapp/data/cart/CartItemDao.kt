package com.example.farmapp.data.cart

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * HU04 - Operaciones locales del carrito.
 */
@Dao
interface CartItemDao {

    @Query(
        "SELECT * FROM cart_items " +
                "WHERE userId = :userId " +
                "ORDER BY name COLLATE NOCASE ASC"
    )
    fun observeCart(
        userId: String
    ): Flow<List<CartItemEntity>>

    @Query(
        "SELECT * FROM cart_items " +
                "WHERE userId = :userId AND sku = :sku " +
                "LIMIT 1"
    )
    suspend fun findItem(
        userId: String,
        sku: String
    ): CartItemEntity?

    @Upsert
    suspend fun upsert(
        item: CartItemEntity
    )

    /**
     * Suma unidades sin superar el stock conocido.
     *
     * La transaccion impide que dos solicitudes
     * concurrentes lean la misma cantidad anterior
     * y terminen sobrescribiendose.
     */
    @Transaction
    suspend fun addOrIncrement(
        item: CartItemEntity,
        availableStock: Int
    ): Boolean {

        if (
            item.quantity <= 0 ||
            availableStock <= 0
        ) {
            return false
        }

        val previous = findItem(
            userId = item.userId,
            sku = item.sku
        )

        val currentQuantity = previous?.quantity ?: 0

        if (
            currentQuantity >= availableStock ||
            item.quantity > availableStock - currentQuantity
        ) {
            return false
        }

        upsert(
            item.copy(
                quantity = currentQuantity + item.quantity
            )
        )

        return true
    }

    @Query(
        "UPDATE cart_items " +
                "SET quantity = :quantity " +
                "WHERE userId = :userId AND sku = :sku"
    )
    suspend fun updateQuantity(
        userId: String,
        sku: String,
        quantity: Int
    ): Int

    @Query(
        "DELETE FROM cart_items " +
                "WHERE userId = :userId AND sku = :sku"
    )
    suspend fun deleteItem(
        userId: String,
        sku: String
    ): Int

    @Query(
        "DELETE FROM cart_items WHERE userId = :userId"
    )
    suspend fun clearCart(
        userId: String
    )
}