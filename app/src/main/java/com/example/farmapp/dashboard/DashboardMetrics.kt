package com.example.farmapp.dashboard

import com.example.farmapp.data.catalog.CatalogProductEntity
import com.example.farmapp.data.orders.FarmOrder
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

/** Valores calculados exclusivamente a partir de los datos que devuelve FarmApp. */
data class CategoryInventory(
    val name: String,
    val productCount: Int,
    val availableUnits: Long
)

data class InventoryMetrics(
    val totalProducts: Int,
    val outOfStock: Int,
    val lowStock: Int,
    val totalUnits: Long,
    val categories: List<CategoryInventory>
)

data class CategoryPriceMetrics(
    val name: String,
    val productCount: Int,
    val averagePriceCents: Long
)

data class CatalogMetrics(
    val totalProducts: Int,
    val availableProducts: Int,
    val averagePriceCents: Long,
    val categories: List<CategoryPriceMetrics>,
    val priceRanges: List<Pair<String, Int>>
)

data class MonthlyOrders(
    val month: YearMonth,
    val count: Int,
    val amountCents: Long
)

data class OrdersMetrics(
    val count: Int,
    val totalCents: Long,
    val latest: FarmOrder?,
    val monthly: List<MonthlyOrders>
)

object DashboardMetrics {
    /** Bajo stock significa entre 1 y 5 unidades, inclusive. */
    fun inventory(products: List<CatalogProductEntity>): InventoryMetrics {
        val active = products.filter { it.active }
        return InventoryMetrics(
            totalProducts = active.size,
            outOfStock = active.count { it.stock == 0 },
            lowStock = active.count { it.stock in 1..5 },
            totalUnits = active.sumOf { it.stock.toLong() },
            categories = active.groupBy { it.category }
                .map { (name, items) ->
                    CategoryInventory(
                        name = name,
                        productCount = items.size,
                        availableUnits = items.sumOf { it.stock.toLong() }
                    )
                }
                .sortedWith(compareByDescending<CategoryInventory> { it.productCount }.thenBy { it.name })
        )
    }

    fun catalog(products: List<CatalogProductEntity>): CatalogMetrics {
        val active = products.filter { it.active }
        val average = if (active.isEmpty()) 0L
        else (active.sumOf { it.priceCents.toDouble() } / active.size).toLong()

        return CatalogMetrics(
            totalProducts = active.size,
            availableProducts = active.count { it.stock > 0 },
            averagePriceCents = average,
            categories = active.groupBy { it.category }
                .map { (name, items) ->
                    CategoryPriceMetrics(
                        name = name,
                        productCount = items.size,
                        averagePriceCents = (items.sumOf { it.priceCents.toDouble() } / items.size).toLong()
                    )
                }
                .sortedBy { it.name },
            priceRanges = listOf(
                "Menos de S/ 10" to active.count { it.priceCents < 1000L },
                "S/ 10 a 29.99" to active.count { it.priceCents in 1000L..2999L },
                "S/ 30 a 59.99" to active.count { it.priceCents in 3000L..5999L },
                "S/ 60 o más" to active.count { it.priceCents >= 6000L }
            )
        )
    }

    /**
     * La evolución considera los últimos seis meses del dispositivo.
     * Los pedidos se obtienen del repositorio Firestore del usuario, no de toda la tienda.
     */
    fun orders(
        orders: List<FarmOrder>,
        today: YearMonth = YearMonth.now(),
        zone: ZoneId = ZoneId.systemDefault()
    ): OrdersMetrics {
        val valid = orders.filter { it.isStructurallyValid() }
            .sortedByDescending { it.createdAtMillis }
        val byMonth = valid.filter { it.createdAtMillis > 0L }
            .groupBy {
                YearMonth.from(Instant.ofEpochMilli(it.createdAtMillis).atZone(zone))
            }
        val months = (5 downTo 0).map { monthsAgo ->
            val month = today.minusMonths(monthsAgo.toLong())
            val items = byMonth[month].orEmpty()
            MonthlyOrders(
                month = month,
                count = items.size,
                amountCents = items.sumOf { it.totalCents }
            )
        }
        return OrdersMetrics(
            count = valid.size,
            totalCents = valid.sumOf { it.totalCents },
            latest = valid.firstOrNull(),
            monthly = months
        )
    }
}
