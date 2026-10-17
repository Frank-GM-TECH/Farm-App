package com.example.farmapp.ui.navigation

/** Cotejo 16: rutas centralizadas de Navigation Compose, como en el codelab. */
object FarmAppDestinations {
    const val AUTH = "auth"
    const val HOME = "home"
    const val CATALOG = "catalog"
    const val REMOTE_GALLERY = "remote_gallery"
    const val SQL_LAB = "sql_lab"
    const val ROOM_DEMO = "room_demo"
    const val BACKGROUND_REPORT = "background_report"
    const val ROOM_ITEM_ARG = "itemId"
    const val ROOM_ITEM_DETAIL = "room_demo/{$ROOM_ITEM_ARG}"
    const val ROOM_ITEM_EDIT = "room_demo/{$ROOM_ITEM_ARG}/edit"
    fun roomDetailRoute(id: Long): String = "room_demo/$id"
    fun roomEditRoute(id: Long): String = "room_demo/$id/edit"
    const val PRODUCT_ARG = "sku"
    const val PRODUCT = "product/{$PRODUCT_ARG}"

    /** Evitar pasar objetos completos. El identificador se limita a SKUs locales. */
    fun productRoute(sku: String): String = "product/${android.net.Uri.encode(sku)}"
}
