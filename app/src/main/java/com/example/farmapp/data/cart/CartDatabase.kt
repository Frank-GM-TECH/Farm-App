package com.example.farmapp.data.cart

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * HU04 - Base de datos local del carrito.
 *
 * Se mantendra separada del catalogo HU03.
 */
@Database(
    entities = [CartItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CartDatabase : RoomDatabase() {

    abstract fun cartItemDao(): CartItemDao
}