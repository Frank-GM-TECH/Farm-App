package com.example.farmapp.data.orders

/** Datos inmutables de los artículos en el instante de crear un pedido. */
data class OrderLine(
    val sku: String = "",
    val name: String = "",
    val unitPriceCents: Long = 0L,
    val quantity: Int = 0
)

/**
 * Pedido de demostración. Nunca contiene datos de tarjetas ni credenciales.
 * Los importes se expresan en céntimos de sol.
 * El servidor no verifica precios, stock, promociones ni identidad fiscal aún.
 */
data class FarmOrder(
    val id: String = "",
    val userId: String = "",
    val createdAtMillis: Long = 0L,
    val items: List<OrderLine> = emptyList(),
    val subtotalCents: Long = 0L,
    val discountCents: Long = 0L,
    val totalCents: Long = 0L,
    val deliveryMethod: String = "",
    val deliveryAddress: String? = null,
    val pickupStoreId: String? = null,
    val paymentMethod: String = "",
    val receiptType: String = "",
    val invoiceRuc: String? = null,
    val invoiceBusinessName: String? = null,
    val couponCode: String? = null,
    val status: String = "DEMO_PENDING"
) {
    fun isStructurallyValid(): Boolean {
        if (id.isBlank() || userId.isBlank() || items.isEmpty()) return false
        if (items.any { it.sku.isBlank() || it.quantity <= 0 || it.unitPriceCents < 0 }) return false
        if (subtotalCents < 0 || discountCents < 0 || discountCents > subtotalCents) return false
        if (totalCents != subtotalCents - discountCents) return false
        if (paymentMethod.isBlank() || receiptType.isBlank()) return false
        if (deliveryMethod == "DELIVERY" && deliveryAddress.isNullOrBlank()) return false
        if (deliveryMethod == "PICKUP" && pickupStoreId.isNullOrBlank()) return false
        if (deliveryMethod != "DELIVERY" && deliveryMethod != "PICKUP") return false
        return try {
            val calculated = items.fold(0L) { sum, line ->
                Math.addExact(sum, Math.multiplyExact(line.unitPriceCents, line.quantity.toLong()))
            }
            calculated == subtotalCents
        } catch (_: ArithmeticException) {
            false
        }
    }
}