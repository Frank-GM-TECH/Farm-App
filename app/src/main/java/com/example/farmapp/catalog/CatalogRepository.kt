package com.example.farmapp.catalog

/** Genéricos: el mismo contrato sirve para DemoProduct o futuros productos reales. */
interface CatalogRepository<T : CatalogItem> {
    fun all(): List<T>
}

class InMemoryCatalogRepository<T : CatalogItem>(items: List<T>) : CatalogRepository<T> {
    private val safeItems = items.toList() // Copia: no exponer una lista mutable externa.
    override fun all(): List<T> = safeItems
}

/** Singleton: una fuente local compartida exclusivamente para la demostración educativa. */
object DemoCatalogData {
    val products: List<DemoProduct> = listOf(
        DemoProduct(DemoProduct.normalizedSku("  fa-001 "), "Alcohol en gel", DemoCategory.HIGIENE, 890, 12),
        DemoProduct("FA-002", "Jabón neutro", DemoCategory.HIGIENE, 650, 7),
        DemoProduct("FA-003", "Mascarillas", DemoCategory.HIGIENE, 480, 0),
        DemoProduct("FA-004", "Gasas estériles", DemoCategory.PRIMEROS_AUXILIOS, 550, 4),
        DemoProduct("FA-005", "Venda elástica", DemoCategory.PRIMEROS_AUXILIOS, 990, 3),
        DemoProduct("FA-006", "Termómetro digital", DemoCategory.EQUIPOS, 2550, 8)
    ).also { items ->
        // Función de alcance also: proteger la coherencia de la lista de ejemplo.
        require(items.map { it.sku }.toSet().size == items.size) { "SKU duplicado" }
    }

    val repository: CatalogRepository<DemoProduct> = InMemoryCatalogRepository(products)
}
