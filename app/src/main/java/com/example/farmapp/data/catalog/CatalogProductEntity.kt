package com.example.farmapp.data.catalog

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * HU03 - Producto almacenado localmente en Room.
 *
 * Cada producto tiene un SKU único y opcionalmente
 * un código de barras para buscarlo con la cámara.
 *
 * Los códigos de barras se guardan como String
 * para conservar posibles ceros iniciales.
 */
@Entity(
    tableName = "catalog_products",
    indices = [
        Index(
            value = ["barcode"],
            unique = true
        )
    ]
)
data class CatalogProductEntity(

    @PrimaryKey
    val sku: String,

    val barcode: String? = null,

    val name: String,

    val description: String = "",

    val category: String,

    val priceCents: Long,

    val stock: Int,

    val imageUrl: String? = null,

// Estado de publicación en Firebase.
    val active: Boolean = true

)
