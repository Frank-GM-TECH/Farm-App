package com.example.farmapp.data.orders

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderModelsTest {
    private fun order() = FarmOrder(
        id = "demo-1", userId = "uid-1",
        items = listOf(OrderLine("SKU1", "Producto", 1200L, 2)),
        subtotalCents = 2400L,
        discountCents = 240L,
        totalCents = 2160L,
        deliveryMethod = "DELIVERY",
        deliveryAddress = "Av. Ejemplo 100",
        paymentMethod = "Yape / Plin",
        receiptType = "Boleta simple"
    )

    @Test fun validOrder() { assertTrue(order().isStructurallyValid()) }
    @Test fun invalidTotal() { assertFalse(order().copy(totalCents = 2000).isStructurallyValid()) }
    @Test fun emptyDeliveryAddress() { assertFalse(order().copy(deliveryAddress = null).isStructurallyValid()) }
    @Test fun discountExceedsSubtotal() { assertFalse(order().copy(discountCents = 2500).isStructurallyValid()) }
}