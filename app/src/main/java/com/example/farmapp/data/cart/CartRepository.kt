package com.example.farmapp.data.cart

import com.example.farmapp.data.catalog.CatalogProductRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * HU04 - Contrato del carrito de compras.
 */
interface CartRepository {

    fun observeCart(
        userId: String
    ): Flow<List<CartItem>>

    suspend fun addProduct(
        userId: String,
        sku: String,
        quantity: Int = 1
    ): Boolean

    suspend fun changeQuantity(
        userId: String,
        sku: String,
        quantity: Int
    ): Boolean

    suspend fun removeProduct(
        userId: String,
        sku: String
    )

    suspend fun clearCart(
        userId: String
    )
}

/**
 * Implementacion offline con Room.
 *
 * La cuenta autenticada se comprueba antes
 * de leer o modificar el carrito.
 */
class OfflineCartRepository(
    private val dao: CartItemDao,
    private val catalogRepository: CatalogProductRepository,
    private val firebaseAuth: FirebaseAuth
) : CartRepository {

    private fun requireCurrentUser(userId: String) {
        require(userId.isNotBlank()) {
            "El identificador de usuario es obligatorio."
        }

        check(firebaseAuth.currentUser?.uid == userId) {
            "La sesion del usuario no es valida."
        }
    }

    override fun observeCart(
        userId: String
    ): Flow<List<CartItem>> {

        requireCurrentUser(userId)

        return dao.observeCart(userId).map { entities ->
            requireCurrentUser(userId)
            entities.map { it.toCartItem() }
        }
    }

    override suspend fun addProduct(
        userId: String,
        sku: String,
        quantity: Int
    ): Boolean {

        requireCurrentUser(userId)
        require(quantity > 0) {
            "La cantidad debe ser mayor que cero."
        }

        val product = catalogRepository.findBySku(sku)
            ?: return false

        requireCurrentUser(userId)

        if (
            product.stock <= 0 ||
            product.priceCents < 0
        ) {
            return false
        }

        val item = CartItemEntity(
            userId = userId,
            sku = product.sku,
            name = product.name,
            imageUrl = product.imageUrl,
            unitPriceCents = product.priceCents,
            quantity = quantity
        )

        return dao.addOrIncrement(
            item = item,
            availableStock = product.stock
        )
    }

    override suspend fun changeQuantity(
        userId: String,
        sku: String,
        quantity: Int
    ): Boolean {

        requireCurrentUser(userId)

        if (quantity < 0) {
            return false
        }

        if (quantity == 0) {
            return dao.deleteItem(userId, sku) > 0
        }

        val product = catalogRepository.findBySku(sku)
            ?: return false

        requireCurrentUser(userId)

        if (quantity > product.stock) {
            return false
        }

        return dao.updateQuantity(
            userId = userId,
            sku = sku,
            quantity = quantity
        ) > 0
    }

    override suspend fun removeProduct(
        userId: String,
        sku: String
    ) {
        requireCurrentUser(userId)
        dao.deleteItem(userId, sku)
    }

    override suspend fun clearCart(
        userId: String
    ) {
        requireCurrentUser(userId)
        dao.clearCart(userId)
    }
}