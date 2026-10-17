package com.example.farmapp.catalog

/** Datos locales para aprender Kotlin; NO son existencias, precios ni ofertas reales. */
enum class DemoCategory { HIGIENE, PRIMEROS_AUXILIOS, EQUIPOS }

enum class StockStatus { AGOTADO, BAJO, DISPONIBLE }

/** Interfaz común para modelos de catálogo. Permite repositorios genéricos. */
interface CatalogItem {
    val sku: String
    val name: String
    val category: DemoCategory
    val priceCents: Int
    val stock: Int
}

data class DemoProduct(
    override val sku: String,
    override val name: String,
    override val category: DemoCategory,
    override val priceCents: Int,
    override val stock: Int
) : CatalogItem {
    init {
        require(sku.isNotBlank() && name.isNotBlank()) { "SKU y nombre obligatorios" }
        require(priceCents >= 0 && stock >= 0) { "Precio o stock negativo" }
    }

    companion object {
        /** Companion object: normaliza un identificador antes de crearlo. */
        fun normalizedSku(code: String): String = code.trim().uppercase()
    }
}

/** Función de extensión reutilizable en HU03 (catálogo) y HU06 (inventario). */
fun DemoProduct.isAvailable(): Boolean = stock > 0

/** Array de umbrales de stock del DEMO. No es política real de inventario. */
object DemoStockPolicy {
    private val thresholds: IntArray = intArrayOf(0, 5)

    fun status(stock: Int): StockStatus = when {
        stock <= thresholds[0] -> StockStatus.AGOTADO
        stock in 1..thresholds[1] -> StockStatus.BAJO
        else -> StockStatus.DISPONIBLE
    }
}
