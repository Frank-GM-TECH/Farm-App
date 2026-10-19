package com.example.farmapp.data.catalog

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * HU03 - Base de datos local del catálogo.
 *
 * Almacena productos para consultas sin conexión.
 *
 * Es independiente de RoomDemoDatabase,
 * utilizada en los ejercicios anteriores.
 */
@Database(
    entities = [CatalogProductEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CatalogDatabase : RoomDatabase() {

    abstract fun catalogProductDao(): CatalogProductDao

}