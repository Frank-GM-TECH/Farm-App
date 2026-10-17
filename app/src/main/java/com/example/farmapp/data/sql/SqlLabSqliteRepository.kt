package com.example.farmapp.data.sql

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** BD LOCAL de práctica. No reemplaza el catálogo, ni Firebase, ni gestiona ventas. */
class SqlLabDatabase(
    context: Context,
    databaseName: String = "farmapp_laboratorio_sql.db"
) : SQLiteOpenHelper(context.applicationContext, databaseName, null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE productos_demo (
                sku TEXT PRIMARY KEY NOT NULL,
                nombre TEXT NOT NULL,
                categoria TEXT NOT NULL,
                stock INTEGER NOT NULL CHECK(stock >= 0)
            )
        """.trimIndent())
        insertInitialExamples(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Solo datos de práctica. Para datos reales se usarán migraciones Room.
        db.execSQL("DROP TABLE IF EXISTS productos_demo")
        onCreate(db)
    }

    companion object {
        fun insertInitialExamples(db: SQLiteDatabase) {
            listOf(
                SqlLabProduct("FA-001", "Alcohol en gel", "Higiene", 12),
                SqlLabProduct("FA-002", "Jabón neutro", "Higiene", 7),
                SqlLabProduct("FA-003", "Mascarillas", "Higiene", 0),
                SqlLabProduct("FA-004", "Gasas estériles", "Primeros auxilios", 4),
                SqlLabProduct("FA-005", "Venda elástica", "Primeros auxilios", 3),
                SqlLabProduct("FA-006", "Termómetro digital", "Equipos", 8)
            ).forEach { p ->
                db.insertOrThrow("productos_demo", null, ContentValues().apply {
                    put("sku", p.sku)
                    put("nombre", p.name)
                    put("categoria", p.category)
                    put("stock", p.stock)
                })
            }
        }
    }
}

/** Las sentencias SQLite se ejecutan en Dispatchers.IO, nunca en el hilo principal. */
class SqliteSqlLabRepository(
    private val helper: SqlLabDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SqlLabRepository {
    override suspend fun runQuery(query: SqlLabQuery): SqlLabResult = withContext(dispatcher) {
        helper.readableDatabase.rawQuery(query.statement, query.arguments).use { cursor ->
            when (query) {
                SqlLabQuery.DISTINCT_CATEGORIES -> SqlLabResult.Categories(buildList {
                    while (cursor.moveToNext()) add(cursor.getString(0))
                })
                SqlLabQuery.GROUP_BY_CATEGORY -> SqlLabResult.Groups(buildList {
                    while (cursor.moveToNext()) add(SqlLabCategoryTotal(
                        category = cursor.getString(0),
                        products = cursor.getInt(1),
                        totalUnits = cursor.getInt(2)
                    ))
                })
                else -> SqlLabResult.Products(buildList {
                    while (cursor.moveToNext()) add(SqlLabProduct(
                        sku = cursor.getString(0),
                        name = cursor.getString(1),
                        category = cursor.getString(2),
                        stock = cursor.getInt(3)
                    ))
                })
            }
        }
    }

    override suspend fun insertExample(): Boolean = withContext(dispatcher) {
        val values = ContentValues().apply {
            put("sku", "FA-SQL-EXTRA")
            put("nombre", "Botiquín de práctica")
            put("categoria", "Primeros auxilios")
            put("stock", 2)
        }
        helper.writableDatabase.insertWithOnConflict(
            "productos_demo", null, values, SQLiteDatabase.CONFLICT_IGNORE
        ) != -1L
    }

    override suspend fun updateExample(): Int = withContext(dispatcher) {
        helper.writableDatabase.update(
            "productos_demo", ContentValues().apply { put("stock", 11) },
            "sku = ?", arrayOf("FA-SQL-EXTRA")
        )
    }

    override suspend fun deleteExample(): Int = withContext(dispatcher) {
        helper.writableDatabase.delete(
            "productos_demo", "sku = ?", arrayOf("FA-SQL-EXTRA")
        )
    }

    override suspend fun resetExamples(): Unit = withContext(dispatcher) {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            db.delete("productos_demo", null, null)
            SqlLabDatabase.insertInitialExamples(db)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
