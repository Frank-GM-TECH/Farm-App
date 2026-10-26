package com.example.farmapp.ui.catalog

import androidx.annotation.DrawableRes
import com.example.farmapp.R

/**
 * Fotografías de catálogo locales para los seis SKU de FarmApp.
 * Para otros SKU se mantiene el imageUrl remoto como respaldo.
 * No modifica Firestore, Room, códigos de barras ni pedidos.
 */
@DrawableRes
fun farmAppProductImage(sku: String): Int? = when (sku) {
    "FA-001" -> R.drawable.farm_product_fa_001
    "FA-002" -> R.drawable.farm_product_fa_002
    "FA-003" -> R.drawable.farm_product_fa_003
    "FA-004" -> R.drawable.farm_product_fa_004
    "FA-005" -> R.drawable.farm_product_fa_005
    "FA-006" -> R.drawable.farm_product_fa_006
    else -> null
}
