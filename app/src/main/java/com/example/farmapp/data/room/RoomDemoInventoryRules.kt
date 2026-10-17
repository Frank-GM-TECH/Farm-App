package com.example.farmapp.data.room

/** Regla del codelab Inventory: no se pueden vender artículos agotados. */
object RoomDemoInventoryRules {
    fun canSell(item: RoomDemoItem?): Boolean = item != null && item.stock > 0
    fun validId(id: Long): Boolean = id > 0
}
