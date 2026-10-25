
package com.example.farmapp.data.orders

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

/**
 * HU04 - C6.2
 * Datos enviados al servidor.
 * Nunca enviamos precios ni stock calculados por Android.
 */
data class CreateOrderItemInput(
    val sku: String,
    val quantity: Int
)

data class CreateOrderRequest(
    val requestId: String,
    val items: List<CreateOrderItemInput>,
    val deliveryMethod: String,
    val paymentMethod: String,
    val receiptType: String,
    val couponCode: String? = null,
    val deliveryAddress: String? = null,
    val pickupStoreId: String? = null,
    val invoiceRuc: String? = null,
    val invoiceBusinessName: String? = null
)

/**
 * Resultado confirmado por Cloud Functions.
 * Los importes se expresan en céntimos.
 */
data class CreateOrderResult(
    val orderId: String,
    val subtotalCents: Long,
    val discountCents: Long,
    val totalCents: Long,
    val reused: Boolean
)

interface CreateOrderRepository {
    suspend fun createOrder(
        request: CreateOrderRequest
    ): CreateOrderResult
}

/**
 * Envía pedidos mediante Firebase Callable Functions.
 *
 * Firebase SDK adjunta automáticamente la autenticación
 * del usuario actual cuando está disponible.
 */
class FirebaseCreateOrderRepository(
    private val functions: FirebaseFunctions =
        FirebaseFunctions.getInstance("us-central1"),
    private val auth: FirebaseAuth =
        FirebaseAuth.getInstance()
) : CreateOrderRepository {

    override suspend fun createOrder(
        request: CreateOrderRequest
    ): CreateOrderResult {

        val currentUid = auth.currentUser?.uid
            ?: error("Debes iniciar sesión para comprar.")

        require(request.requestId.isNotBlank()) {
            "El pedido necesita un identificador."
        }

        require(request.items.isNotEmpty()) {
            "El carrito no puede estar vacío."
        }

        require(request.items.all { it.quantity > 0 }) {
            "Las cantidades deben ser positivas."
        }

        val payload = mapOf<String, Any?>(
            "requestId" to request.requestId,

            "items" to request.items.map { item ->
                mapOf(
                    "sku" to item.sku,
                    "quantity" to item.quantity
                )
            },

            "couponCode" to request.couponCode,
            "deliveryMethod" to request.deliveryMethod,
            "deliveryAddress" to request.deliveryAddress,
            "pickupStoreId" to request.pickupStoreId,
            "paymentMethod" to request.paymentMethod,
            "receiptType" to request.receiptType,
            "invoiceRuc" to request.invoiceRuc,
            "invoiceBusinessName" to request.invoiceBusinessName
        )

        val response = functions
            .getHttpsCallable("createOrder")
            .call(payload)
            .await()

        check(auth.currentUser?.uid == currentUid) {
            "La sesión cambió durante el pedido."
        }

        val result = response.data as? Map<*, *>
            ?: error("Respuesta inesperada del servidor.")

        check(
            result["validated"] == true &&
                    result["persisted"] == true
        ) {
            "El servidor no confirmó el registro."
        }

        val orderId = result["orderId"] as? String
            ?: error("La respuesta no contiene el pedido.")

        check(orderId == request.requestId) {
            "El identificador devuelto no coincide."
        }

        val subtotal = (result["subtotalCents"] as? Number)
            ?.toLong()
            ?: error("Subtotal no recibido.")

        val discount = (result["discountCents"] as? Number)
            ?.toLong()
            ?: error("Descuento no recibido.")

        val total = (result["totalCents"] as? Number)
            ?.toLong()
            ?: error("Total no recibido.")

        check(
            subtotal >= 0L &&
                    discount >= 0L &&
                    discount <= subtotal &&
                    total == subtotal - discount
        ) {
            "Los importes recibidos son inconsistentes."
        }

        val reused = result["reused"] as? Boolean
            ?: error("El servidor no informó el estado del pedido.")

        return CreateOrderResult(
            orderId = orderId,
            subtotalCents = subtotal,
            discountCents = discount,
            totalCents = total,
            reused = reused
        )
    }
}