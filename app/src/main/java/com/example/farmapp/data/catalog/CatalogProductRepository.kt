package com.example.farmapp.data.catalog

import kotlinx.coroutines.flow.Flow

/**
 * HU03 - Repositorio del catálogo.
 *
 * Separa el acceso a Room de los ViewModels
 * y de las pantallas de Jetpack Compose.
 */
interface CatalogProductRepository {

    // Observar los productos guardados en Room.
    val products: Flow<List<CatalogProductEntity>>

    // Buscar un producto por SKU.
    suspend fun findBySku(
        sku: String
    ): CatalogProductEntity?

    // Buscar un producto por código de barras.
    suspend fun findByBarcode(
        barcode: String
    ): CatalogProductEntity?

    // Guardar o actualizar un producto.
    suspend fun saveProduct(
        product: CatalogProductEntity
    )

    // Guardar o actualizar varios productos.
    suspend fun saveProducts(
        products: List<CatalogProductEntity>
    )

    // HU03 - Insertar datos de prueba solo si Room está vacío.
    suspend fun initializeSampleProducts()
}

/**
 * Implementación del repositorio utilizando Room.
 */
class OfflineCatalogProductRepository(
    private val dao: CatalogProductDao
) : CatalogProductRepository {

    override val products: Flow<List<CatalogProductEntity>> =
        dao.observeAll()

    override suspend fun findBySku(
        sku: String
    ): CatalogProductEntity? {
        return dao.findBySku(sku)
    }

    override suspend fun findByBarcode(
        barcode: String
    ): CatalogProductEntity? {
        return dao.findByBarcode(barcode)
    }

    override suspend fun saveProduct(
        product: CatalogProductEntity
    ) {
        require(
            product.sku.isNotBlank() &&
                    product.name.isNotBlank() &&
                    product.category.isNotBlank() &&
                    product.priceCents >= 0 &&
                    product.stock >= 0
        ) {
            "El producto contiene datos inválidos."
        }

        dao.upsert(product)
    }

    override suspend fun saveProducts(
        products: List<CatalogProductEntity>
    ) {
        require(products.all {
            it.sku.isNotBlank() &&
                    it.name.isNotBlank() &&
                    it.category.isNotBlank() &&
                    it.priceCents >= 0 &&
                    it.stock >= 0
        }) {
            "La lista contiene productos inválidos."
        }

        dao.upsertAll(products)
    }

    // =========================================
    // HU03 - CARGA INICIAL DE PRODUCTOS
    // =========================================

    override suspend fun initializeSampleProducts() {

        // Obtiene los seis productos de demostración.
        val sampleProducts = CatalogSeedData.sampleProducts

        // Solo los inserta si la tabla está vacía.
        // No modifica datos que ya existan.
        dao.seedIfEmpty(sampleProducts)
    }
}