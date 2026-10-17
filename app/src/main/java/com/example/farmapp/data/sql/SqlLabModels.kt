package com.example.farmapp.data.sql

/** C24: MODELO educativo aislado de la farmacia e inventario real. */
data class SqlLabProduct(val sku: String, val name: String, val category: String, val stock: Int)
data class SqlLabCategoryTotal(val category: String, val products: Int, val totalUnits: Int)

/** SQL constante; los valores de usuario se enlazan con ? (nunca concatenación). */
enum class SqlLabQuery(val statement: String, val arguments: Array<String> = emptyArray()) {
    ALL("SELECT sku, nombre, categoria, stock FROM productos_demo ORDER BY nombre COLLATE NOCASE ASC LIMIT 20"),
    LOW_STOCK("SELECT sku, nombre, categoria, stock FROM productos_demo WHERE stock BETWEEN ? AND ? ORDER BY stock ASC, nombre ASC LIMIT 20", arrayOf("1", "5")),
    NAME_LIKE("SELECT sku, nombre, categoria, stock FROM productos_demo WHERE nombre LIKE ? ORDER BY nombre ASC LIMIT 20", arrayOf("%gel%")),
    DISTINCT_CATEGORIES("SELECT DISTINCT categoria FROM productos_demo ORDER BY categoria ASC"),
    GROUP_BY_CATEGORY("SELECT categoria, COUNT(*) AS productos, SUM(stock) AS unidades FROM productos_demo GROUP BY categoria ORDER BY categoria ASC")
}

sealed interface SqlLabResult {
    data class Products(val items: List<SqlLabProduct>) : SqlLabResult
    data class Categories(val items: List<String>) : SqlLabResult
    data class Groups(val items: List<SqlLabCategoryTotal>) : SqlLabResult
}

enum class SqlLabFeedback { NONE, INSERTED, EXISTS, UPDATED, NOT_FOUND, DELETED, RESTORED }

interface SqlLabRepository {
    suspend fun runQuery(query: SqlLabQuery): SqlLabResult
    suspend fun insertExample(): Boolean
    suspend fun updateExample(): Int
    suspend fun deleteExample(): Int
    suspend fun resetExamples()
}
