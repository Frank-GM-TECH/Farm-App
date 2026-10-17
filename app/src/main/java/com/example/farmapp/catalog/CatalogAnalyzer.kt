package com.example.farmapp.catalog

/** Resumen de una consulta. Set y Map evitan duplicados y permiten agrupar productos. */
data class CatalogSnapshot<T : CatalogItem>(
    val products: List<T>,
    val categories: Set<DemoCategory>,
    val grouped: Map<DemoCategory, List<T>>,
    val unitsByCategory: Map<DemoCategory, Int>,
    val totalUnits: Int,
    val totalInventoryValueCents: Long
)

/** Funciones de orden superior y colecciones aplicadas a productos de demostración. */
class CatalogAnalyzer<T : CatalogItem>(private val repository: CatalogRepository<T>) {
    fun search(query: String = "", category: DemoCategory? = null): CatalogSnapshot<T> =
        with(repository.all()) { // scope function with: opera sobre la lista de entrada.
            val normalizedQuery = query.trim().lowercase()
            val categories = map { it.category }.toSet() // List -> Set
            val filtered = filter { item ->
                val matchesName = normalizedQuery.takeIf { it.isNotEmpty() }
                    ?.let { term -> item.name.lowercase().contains(term) || item.sku.lowercase().contains(term) }
                    ?: true // let, llamada segura y Elvis.
                matchesName && (category == null || item.category == category)
            }.sortedBy { it.name.lowercase() }
            val grouped = filtered.groupBy { it.category } // Map<Category, List<T>>
            val byCategory = mutableMapOf<DemoCategory, Int>().apply {
                // forEach + fold, con un Map de unidades por categoría.
                grouped.forEach { (group, products) ->
                    put(group, products.fold(0) { sum, item -> sum + item.stock })
                }
            }.toMap()
            val total = filtered.run { fold(0) { sum, item -> sum + item.stock } } // run: total desde la colección.
            val valueCents = filtered.fold(0L) { sum, item ->
                sum + item.priceCents.toLong() * item.stock
            }
            CatalogSnapshot(filtered, categories, grouped, byCategory, total, valueCents)
        }
}
