package com.example.farmapp.dashboard

import com.example.farmapp.data.catalog.CatalogProductEntity
import com.example.farmapp.data.orders.FarmOrder
import com.example.farmapp.data.orders.OrderLine
import java.time.YearMonth
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardMetricsTest {
    private fun product(
        sku: String,
        category: String,
        price: Long,
        stock: Int,
        active: Boolean = true
    ) = CatalogProductEntity(
        sku = sku,
        name = sku,
        category = category,
        priceCents = price,
        stock = stock,
        active = active
    )

    @Test
    fun inventoryCountsOnlyActiveProductsAndLowStock() {
        val items = listOf(
            product("A", "Cuidado", 1200, 0),
            product("B", "Cuidado", 1300, 5),
            product("C", "Higiene", 900, 6),
            product("D", "Higiene", 2000, 1, active = false)
        )
        val metrics = DashboardMetrics.inventory(items)

        assertEquals(3, metrics.totalProducts)
        assertEquals(1, metrics.outOfStock)
        assertEquals(1, metrics.lowStock)
        assertEquals(11L, metrics.totalUnits)
        assertEquals(2, metrics.categories.size)
        assertEquals(2, metrics.categories.first { it.name == "Cuidado" }.productCount)
    }

    @Test
    fun catalogComputesPriceRangesAndAverageFromActiveProducts() {
        val items = listOf(
            product("A", "Cuidado", 900, 2),
            product("B", "Cuidado", 2100, 0),
            product("C", "Higiene", 6500, 3),
            product("D", "Higiene", 8000, 1, active = false)
        )
        val metrics = DashboardMetrics.catalog(items)

        assertEquals(3, metrics.totalProducts)
        assertEquals(2, metrics.availableProducts)
        assertEquals(3166L, metrics.averagePriceCents)
        assertEquals(listOf(1, 1, 0, 1), metrics.priceRanges.map { it.second })
        assertEquals(1500L, metrics.categories.first { it.name == "Cuidado" }.averagePriceCents)
    }

    @Test
    fun ordersGroupRealValidOrdersByMonth() {
        fun order(id: String, month: YearMonth) = FarmOrder(
            id = id,
            userId = "user-1",
            createdAtMillis = month.atDay(10).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            items = listOf(OrderLine(sku = "SKU", name = "Producto", unitPriceCents = 1200, quantity = 1)),
            subtotalCents = 1200,
            totalCents = 1200,
            deliveryMethod = "PICKUP",
            pickupStoreId = "botica-1",
            paymentMethod = "CASH",
            receiptType = "BOLETA"
        )
        val current = YearMonth.of(2026, 11)
        val metrics = DashboardMetrics.orders(
            listOf(order("p1", current), order("p2", current.minusMonths(2))),
            today = current,
            zone = ZoneOffset.UTC
        )

        assertEquals(2, metrics.count)
        assertEquals(2400L, metrics.totalCents)
        assertEquals("p1", metrics.latest?.id)
        assertEquals(6, metrics.monthly.size)
        assertEquals(1, metrics.monthly.last().count)
        assertEquals(1200L, metrics.monthly.last().amountCents)
    }

    @Test
    fun emptyDashboardsHaveZeroValuesWithoutInventingData() {
        assertEquals(0, DashboardMetrics.inventory(emptyList()).totalProducts)
        assertEquals(0, DashboardMetrics.catalog(emptyList()).totalProducts)
        assertEquals(0, DashboardMetrics.orders(emptyList(), YearMonth.of(2026, 11)).count)
    }
}
