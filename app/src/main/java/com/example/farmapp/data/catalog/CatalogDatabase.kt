package com.example.farmapp.data.catalog

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [CatalogProductEntity::class],
    version = 2,
    exportSchema = false
)
abstract class CatalogDatabase : RoomDatabase() {

    abstract fun catalogProductDao(): CatalogProductDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(
                db: SupportSQLiteDatabase
            ) {
                db.execSQL(
                    """
                    ALTER TABLE catalog_products
                    ADD COLUMN active INTEGER
                    NOT NULL DEFAULT 1
                    """.trimIndent()
                )
            }
        }
    }
}