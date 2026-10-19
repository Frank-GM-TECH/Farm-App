package com.example.farmapp.data.catalog

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * HU03 - Acceso a los productos del catálogo.
 */
@Dao
interface CatalogProductDao {

    // Observar todos los productos.
    @Query(
        "SELECT * FROM catalog_products " +
                "ORDER BY name COLLATE NOCASE ASC"
    )
    fun observeAll(): Flow<List<CatalogProductEntity>>

    // Buscar producto por SKU.
    @Query(
        "SELECT * FROM catalog_products " +
                "WHERE sku = :sku LIMIT 1"
    )
    suspend fun findBySku(
        sku: String
    ): CatalogProductEntity?

    // Buscar producto por código de barras.
    @Query(
        "SELECT * FROM catalog_products " +
                "WHERE barcode = :barcode LIMIT 1"
    )
    suspend fun findByBarcode(
        barcode: String
    ): CatalogProductEntity?

    // Guardar o actualizar un producto.
    @Upsert
    suspend fun upsert(
        product: CatalogProductEntity
    )

    // Guardar o actualizar varios productos.
    @Upsert
    suspend fun upsertAll(
        products: List<CatalogProductEntity>
    )

    // Contar productos almacenados.
    @Query("SELECT COUNT(*) FROM catalog_products")
    suspend fun countProducts(): Int

    // Insertar sin reemplazar registros existentes.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(
        products: List<CatalogProductEntity>
    )

    /**
     * Inicializar el catálogo solamente si está vacío.
     *
     * La transacción evita que la comprobación
     * y la inserción se ejecuten por separado.
     */
    @Transaction
    suspend fun seedIfEmpty(
        products: List<CatalogProductEntity>
    ) {
        if (countProducts() == 0) {
            insertIfMissing(products)
        }
    }
}